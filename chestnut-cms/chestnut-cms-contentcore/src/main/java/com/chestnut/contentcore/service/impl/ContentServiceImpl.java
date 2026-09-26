/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.dao.CmsContentDAO;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.domain.dto.CopyContentDTO;
import com.chestnut.contentcore.domain.dto.MoveContentDTO;
import com.chestnut.contentcore.domain.dto.SetTopContentDTO;
import com.chestnut.contentcore.enums.ContentCoreTips;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentOpType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.*;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.chestnut.contentcore.perms.SitePermissionType.SitePrivItem;
import com.chestnut.contentcore.properties.RepeatTitleCheckProperty;
import com.chestnut.contentcore.publish.IContentPathRule;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.ContentLogUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.fixed.config.BackendContext;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * 内容核心管理服务实现
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements IContentService {

	private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

	private final TransactionTemplate transactionTemplate;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IContentRelaService contentRelaService;

	private final AsyncTaskManager asyncTaskManager;

	private final CmsContentDAO dao;

	private final RedissonClient redissonClient;

	@Override
	public CmsContentDAO dao() {
		return dao;
	}

	@Override
	public AsyncTask deleteContents(List<Long> contentIds, LoginUser operator) {
		Assert.isTrue(contentIds.size() <= 100, () -> ContentCoreErrorCode.BATCH_DEL_CONTENT_LIMIT.exception(100));
		Locale locale = LocaleContextHolder.getLocale();
		final List<CmsContent> contents = this.dao().listByIds(contentIds);
		AsyncTask task = new AsyncTask(locale) {
			@Override
			public void run0() {
				for (int i = 0; i < contents.size(); i++) {
					try {
						CmsContent xContent = contents.get(i);
						this.setProgressInfo((i * 100) / contents.size(), ContentCoreTips.DELETING_CONTENT, xContent.getTitle());
						deleteContent0(xContent, operator, Map.of());
					} catch (GlobalException e) {
						addErrorMessage(I18nUtils.get(e.getErrorCode().value(), this.getLocale(), e.getErrArgs()));
					}
				}
				setProgressInfo(100, ContentCoreTips.DELETE_CONTENTS_SUCCESS);
			}
		};
		task.setType("DeleteContents");
		asyncTaskManager.execute(task);
		return task;
	}

	@Override
	public AsyncTask deleteContent(CmsContent cmsContent, LoginUser loginUser, Map<String, Object> params) {
		Locale locale = LocaleContextHolder.getLocale();
		AsyncTask task = new AsyncTask(locale) {
			@Override
			public void run0() {
				deleteContent0(cmsContent, loginUser, Map.of());
				setProgressInfo(100, ContentCoreTips.DELETE_CONTENTS_SUCCESS);
			}
		};
		task.setType("DeleteContent");
		asyncTaskManager.execute(task);
		return task;
	}

	private void deleteContent0(CmsContent cmsContent, LoginUser loginUser, Map<String, Object> params) {
		String perm = CatalogPrivItem.DeleteContent.getPermissionKey(cmsContent.getCatalogId());
		PermissionUtils.checkPermission(perm, loginUser);
		boolean canDelete = ContentStatus.isDraft(cmsContent.getStatus()) || ContentStatus.isOffline(cmsContent.getStatus());
		Assert.isTrue(canDelete, ContentCoreErrorCode.DEL_CONTENT_ERR::exception);

		IContentType contentType = ContentCoreUtils.getContentType(cmsContent.getContentType());
		IContent<?> content = contentType.loadContent(cmsContent);
		content.setOperator(Operator.of(loginUser));
		content.setParams(params);
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			content.delete();
			contentRelaService.onContentDelete(content.getContentEntity().getContentId());
		});
		SpringUtils.publishEvent(new AfterContentDeleteEvent(this, content));
		// 删除映射内容
		List<CmsContent> mappingList = this.dao().lambdaQuery()
				.eq(CmsContent::getCopyType, ContentCopyType.Mapping)
				.eq(CmsContent::getCopyId, content.getContentEntity().getContentId())
				.list();
		for (CmsContent mappingContent : mappingList) {
			log.debug("CC.Content[{}].delete: mapping content delete", content.getContentEntity().getContentId());
			try {
				IContentType mappingContentType = ContentCoreUtils.getContentType(cmsContent.getContentType());
				IContent<?> mappingIContent = mappingContentType.loadContent(cmsContent);
				mappingIContent.setOperator(Operator.of(loginUser));
				mappingIContent.setParams(params);
				transactionTemplate.executeWithoutResult(transactionStatus -> {
					mappingIContent.delete();
					contentRelaService.onContentDelete(mappingIContent.getContentEntity().getContentId());
				});
				SpringUtils.publishEvent(new AfterContentDeleteEvent(this, mappingIContent));
			} catch (Exception e) {
				AsyncTaskManager.setTaskTenPercentProgressInfo(ContentCoreTips.DELETING_MAPPING_CONTENT.locale(
						mappingContent.getTitle(), mappingContent.getContentId()));
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverContents(List<Long> backupIds, LoginUser operator) {
		Map<Long, Integer> catalogContentIncr = new HashMap<>();
		List<BCmsContent> backupContents = this.dao().getBackupByIds(backupIds);
		for (BCmsContent backupContent : backupContents) {
			LoginUser loginUser = StpAdminUtil.getLoginUser();
			PermissionUtils.checkPermission(CatalogPrivItem.AddContent.getPermissionKey(backupContent.getCatalogId()), loginUser);

			IContentType contentType = ContentCoreUtils.getContentType(backupContent.getContentType());
			contentType.recover(backupContent);

			catalogContentIncr.put(
					backupContent.getCatalogId(),
					catalogContentIncr.getOrDefault(backupContent.getCatalogId(), 0) + 1
			);
		}
		catalogContentIncr.forEach(this.catalogService::changeContentCount);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteRecycleContents(List<Long> backupIds) {
		List<BCmsContent> backupContents = this.dao().listBackupContentByIds(backupIds, List.of(
				BCmsContent::getContentId,
				BCmsContent::getContentType
		));
		for (BCmsContent backupContent : backupContents) {
			LoginUser loginUser = StpAdminUtil.getLoginUser();
			PermissionUtils.checkPermission(CatalogPrivItem.DeleteContent.getPermissionKey(backupContent.getCatalogId()), loginUser);

			IContentType contentType = ContentCoreUtils.getContentType(backupContent.getContentType());
			contentType.deleteBackups(backupContent.getContentId());
		}
	}

	@Override
	public void deleteContentsByCatalog(CmsCatalog catalog, boolean includeChild, LoginUser loginUser) {
		long pageSize = 100;
		long total = this.dao().lambdaQuery()
				.eq(!includeChild, CmsContent::getCatalogId, catalog.getCatalogId())
				.likeRight(includeChild, CmsContent::getCatalogAncestors, catalog.getAncestors())
				.count();
		Operator operator = Operator.of(loginUser);
		for (int i = 1; (i - 1) * pageSize < total; i++) {
			AsyncTaskManager.setTaskProgressInfo((int) ((i - 1) * pageSize / total),
					"正在栏目删除内容：" + ((i - 1) * pageSize) + " / " + total);
			this.dao().lambdaQuery()
					.eq(!includeChild, CmsContent::getCatalogId, catalog.getCatalogId())
					.likeRight(includeChild, CmsContent::getCatalogAncestors, catalog.getAncestors())
					.page(new Page<>(i, pageSize, false)).getRecords().forEach(content -> {
						IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
						IContent<?> icontent = contentType.loadContent(content);
						icontent.setOperator(operator);
						icontent.getParams().put(IContent.PARAM_IS_DELETE_BY_CATALOG, true);
						icontent.delete();
					});
		}
	}

    @Override
    public void validateStaticPath(CmsContent content) {
        if (StringUtils.isBlank(content.getStaticPath())) {
            content.setStaticPath(null);
            return;
        }
        try {
            String relative = SiteFilePaths.strictRelative(content.getStaticPath());
            String first = relative.split("/", 2)[0].toLowerCase(Locale.ROOT);
            if (Set.of("template", "include", "assets").contains(first)) {
                throw ContentCoreErrorCode.INVALID_CONTENT_STATIC_PATH.exception();
            }
            CmsSite site = this.siteService.getSite(content.getSiteId());
            for (CmsPublishPipe pipe : this.publishPipeService.getPublishPipes(site.getSiteId())) {
                SiteFilePaths paths = new SiteFilePaths(Path.of(CMSConfig.getResourceRoot()),
                        List.of(site.getPath() + "_" + pipe.getCode()));
                paths.checkFile(paths.resolveRelative(paths.roots().get(0), relative));
            }
            content.setStaticPath(relative);
        } catch (GlobalException e) {
            if (e.getErrorCode() == ContentCoreErrorCode.SITE_FILE_OP_ERR) {
                throw ContentCoreErrorCode.INVALID_CONTENT_STATIC_PATH.exception();
            }
            throw e;
        } catch (IOException e) {
            throw CommonErrorCode.SYSTEM_ERROR.exception(e);
        }
    }

    @Override
    public String getContentStaticPath(CmsContent content, String publishPipeCode) {
        if (content.isLinkContent()) {
            return StringUtils.EMPTY; // 链接内容无静态文件
        }
        CmsSite site = this.siteService.getSite(content.getSiteId());
        CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
        String staticPath = content.getStaticPath();
        if (StringUtils.isEmpty(staticPath)) {
            IContentPathRule rule = ContentCoreUtils.getContentPathRule(catalog.getDetailNameRule());
            String path = Objects.isNull(rule) ? catalog.getPath() : rule.getDirectory(site, catalog, content);
            staticPath = path + content.getContentId() + "." + site.getStaticSuffix(publishPipeCode);
        }
        return staticPath;
    }

	@Override
	public String getContentLink(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (content.isLinkContent()) {
			return InternalUrlUtils.getActualUrl(content.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String previewPath = IInternalDataType.getPreviewPath(InternalDataType_Content.ID, content.getContentId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + previewPath;
		}
		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		String prefix = SiteUtils.getPublishPipePrefix(site, publishPipeCode, isPreview);
		if (catalog.isStaticize()) {
			String contentPath = content.getStaticPath();
			if (StringUtils.isEmpty(contentPath)) {
				IContentPathRule rule = ContentCoreUtils.getContentPathRule(catalog.getDetailNameRule());
				String path = Objects.isNull(rule) ? catalog.getPath() : rule.getDirectory(site, catalog, content);
				contentPath = path + content.getContentId() + "." + site.getStaticSuffix(publishPipeCode);
			}
			return prefix + contentPath;
		} else {
			String viewPath = IInternalDataType.getViewPath(InternalDataType_Content.ID, content.getContentId(),
					publishPipeCode, pageIndex);
			return prefix + viewPath;
		}
	}

	@Override
	public void lock(CmsContent content, String operator) {
		Assert.isFalse(ContentStatus.isFlowing(content.getStatus()), ContentCoreErrorCode.CONTENT_FLOWING::exception);
		boolean checkLock = content.isLock() && StringUtils.isNotEmpty(content.getLockUser())
				&& !Strings.CS.equals(content.getLockUser(), operator);
		Assert.isFalse(checkLock, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getTitle(), content.getLockUser()));

		content.setIsLock(YesOrNo.YES);
		content.setLockUser(operator);
		content.updateBy(operator);
		this.dao().updateById(content);
		ContentLogUtils.addLog(ContentOpType.LOCK, content, AdminUserType.TYPE, operator);
	}

	@Override
	public void unLock(CmsContent content, String operator) {
		Assert.isFalse(ContentStatus.isFlowing(content.getStatus()), ContentCoreErrorCode.CONTENT_FLOWING::exception);
		if (!content.isLock()) {
			return;
		}
		boolean checkOp = StringUtils.isNotEmpty(content.getLockUser())
				&& !Strings.CS.equals(content.getLockUser(), operator);
		Assert.isFalse(checkOp, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getTitle(), content.getLockUser()));
		content.setIsLock(YesOrNo.NO);
		content.setLockUser(StringUtils.EMPTY);
		content.updateBy(operator);
		this.dao().updateById(content);
		ContentLogUtils.addLog(ContentOpType.UNLOCK, content, AdminUserType.TYPE, operator);
	}

	@Override
	public AsyncTask addContent(IContent<?> content) {
		ContentServiceImpl aopProxy = SpringUtils.getAopProxy(this);
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() {
				aopProxy.addContent0(content);
                AsyncTaskManager.setTaskProgressInfo(100, ContentCoreTips.SAVE_SUCCESS, this.getLocale());
			}
		};
		task.setType("SaveContent-" + content.getContentEntity().getContentId());
		asyncTaskManager.execute(task);
		return task;
	}

	@Transactional(rollbackFor = Exception.class)
	public void addContent0(IContent<?> content) {
		content.add();
	}

	@Override
	public AsyncTask saveContent(ContentDTO dto, LoginUser operator) {
		ContentServiceImpl aopProxy = SpringUtils.getAopProxy(this);
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() {
				aopProxy.saveContent0(dto, operator);
                AsyncTaskManager.setTaskProgressInfo(100, ContentCoreTips.SAVE_SUCCESS, this.getLocale());
			}
		};
		task.setType("SaveContent");
		asyncTaskManager.execute(task);
		return task;
	}

	@Override
	public void checkSaveContentPermission(Long contentCatalogId, Long targetCatalogId, LoginUser operator) {
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(contentCatalogId), operator);
		if (!contentCatalogId.equals(targetCatalogId)) {
			// 栏目变更要校验目标栏目的新增内容权限
			PermissionUtils.checkPermission(CatalogPrivItem.AddContent.getPermissionKey(targetCatalogId), operator);
		}
	}

	public void saveContent0(ContentDTO dto, LoginUser operator) {
		RLock lock = redissonClient.getLock(ContentCoreConsts.lockContent(dto.getContentId()));
		if (!lock.tryLock()) {
			throw ContentCoreErrorCode.CONTENT_EDITING.exception();
		}
		try {
			CmsContent cmsContent = this.dao().getById(dto.getContentId());
			this.checkSaveContentPermission(cmsContent.getCatalogId(), dto.getCatalogId(), operator);

			IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
			IContent<?> content = ct.loadContent(cmsContent);
			content.setParams(dto.getParams());
			content.setOperator(Operator.of(operator));

			this.transactionTemplate.executeWithoutResult(__ -> content.save(dto));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public AsyncTask copy(CopyContentDTO dto) {
		List<CmsCatalog> catalogs = dto.getCatalogIds().stream().map(catalogService::getCatalog)
				.filter(Objects::nonNull).toList();
		AsyncTask task = new AsyncTask(LocaleContextHolder.getLocale()) {

			@Override
			public void run0() {
				List<Long> contentIds = dto.getContentIds();
				for (Long contentId : contentIds) {
					try {
						CmsContent cmsContent = dao().getById(contentId);
						if (Objects.nonNull(cmsContent)) {
							if (Objects.nonNull(dto.getSourceSiteId())) {
								Assert.isTrue(Objects.equals(cmsContent.getSiteId(), dto.getSourceSiteId()),
										() -> CommonErrorCode.INVALID_REQUEST_ARG.exception("contentIds"));
							}
							PermissionUtils.checkPermission(CatalogPrivItem.View.getPermissionKey(cmsContent.getCatalogId()), dto.getOperator());
							for (CmsCatalog catalog : catalogs) {
								// 校验权限
								PermissionUtils.checkPermission(SitePrivItem.View.getPermissionKey(catalog.getSiteId()), dto.getOperator());
								PermissionUtils.checkPermission(CatalogPermissionType.CatalogPrivItem.AddContent.getPermissionKey(catalog.getCatalogId()), dto.getOperator());
								boolean crossSite = !Objects.equals(cmsContent.getSiteId(), catalog.getSiteId());
								Assert.isTrue(crossSite == ContentCopyType.isCrossSite(dto.getCopyType()),
										() -> CommonErrorCode.INVALID_REQUEST_ARG.exception("copyType"));
								CmsContent copyContent = copy0(cmsContent, catalog, dto.getCopyType(), dto.getOperator());
								SpringUtils.publishEvent(new AfterContentCopyEvent(this, cmsContent, copyContent));
							}
						}
					} catch (GlobalException e) {
						addErrorMessage(I18nUtils.get(e.getErrorCode().value(), this.getLocale(), e.getErrArgs()));
					}
				}
				this.setProgressInfo(100, ContentCoreTips.COPY_CONTENT_SUCCESS.locale(this.getLocale()));
			}
		};
		task.setType("CopyContent");
		asyncTaskManager.execute(task);
		return task;

	}

	private CmsContent copy0(CmsContent cmsContent, CmsCatalog toCatalog, Integer copyType, LoginUser loginUser) {
		AsyncTaskManager.setTaskTenPercentProgressInfo(ContentCoreTips.COPYING_CONTENT.locale(AsyncTaskManager.getLocale(),
				cmsContent.getTitle(), toCatalog.getName()));
		RLock lock = redissonClient.getLock(ContentCoreConsts.lockContent(cmsContent.getContentId()));
		if (!lock.tryLock()) {
			throw ContentCoreErrorCode.CONTENT_EDITING.exception();
		}
		try {
			this.checkSaveContentPermission(cmsContent.getCatalogId(), toCatalog.getCatalogId(), loginUser);

			IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
			IContent<?> content = ct.loadContent(cmsContent);
			content.setOperator(Operator.of(loginUser));
			return transactionTemplate.execute(transactionStatus -> content.copyTo(toCatalog, copyType));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public AsyncTask move(MoveContentDTO dto) {
		final CmsCatalog catalog = catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));
		AsyncTask task = new AsyncTask(LocaleContextHolder.getLocale()) {

			@Override
			public void run0() {

				List<Long> contentIds = dto.getContentIds();
				for (Long contentId : contentIds) {
					moveContent(contentId, catalog, dto.getOperator());
				}
				this.setProgressInfo(100, ContentCoreTips.MOVE_CONTENT_SUCCESS.locale(this.getLocale()));
			}
		};
		task.setType("MoveContent");
		asyncTaskManager.execute(task);
		return task;
	}

	@Override
	public void moveContent(Long contentId, CmsCatalog toCatalog, LoginUser loginUser) {
		RLock lock = redissonClient.getLock(ContentCoreConsts.lockContent(contentId));
		if (!lock.tryLock()) {
			throw ContentCoreErrorCode.CONTENT_EDITING.exception();
		}
		try {
			CmsContent cmsContent = this.dao().getById(contentId);
			if (Objects.isNull(cmsContent)) {
				AsyncTaskManager.addErrMessage(ContentCoreTips.CONTENT_NOT_FOUND_BY_ID, contentId);
				return;
			}
			Assert.notNull(contentId, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
			if (cmsContent.getCatalogId().equals(toCatalog.getCatalogId())) {
				log.warn("Cannot move content to source catalog!");
				return;
			}
			// 校验权限
			this.checkSaveContentPermission(cmsContent.getCatalogId(), toCatalog.getCatalogId(), loginUser);
			AsyncTaskManager.setTaskTenPercentProgressInfo(ContentCoreTips.MOVING_CONTENT.locale(AsyncTaskManager.getLocale(),
					cmsContent.getTitle(), toCatalog.getName()));
			IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
			IContent<?> content = ct.loadContent(cmsContent);
			content.setOperator(Operator.of(loginUser));
			transactionTemplate.executeWithoutResult(transactionStatus -> content.moveTo(toCatalog));
			SpringUtils.publishEvent(new AfterContentMoveEvent(this, toCatalog, cmsContent));
		}  finally {
			lock.unlock();
		}
	}

	@Override
	public void setTop(SetTopContentDTO dto) {
		List<CmsContent> contents = this.dao().listByIds(dto.getContentIds());
		// 先校验内容编辑权限
		for (CmsContent c : contents) {
			PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(c.getCatalogId()), dto.getOperator());

			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(Operator.of(dto.getOperator()));
			transactionTemplate.executeWithoutResult(transactionStatus -> content.setTop(dto.getTopEndTime()));
			SpringUtils.publishEvent(new AfterContentTopSetEvent(this, content));
		}
	}

	@Override
	public void cancelTop(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.dao().lambdaQuery()
				.gt(CmsContent::getTopFlag, 0)
				.in(CmsContent::getContentId, contentIds)
				.list();
		for (CmsContent c : contents) {
			// 先校验内容编辑权限
			PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(c.getCatalogId()), operator);

			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(Operator.of(operator));
			transactionTemplate.executeWithoutResult(transactionStatus -> content.cancelTop());
			SpringUtils.publishEvent(new AfterContentTopCancelEvent(this, content));
		}
	}

	@Override
	public AsyncTask offline(List<Long> contentIds, LoginUser operator) {
		Locale locale = LocaleContextHolder.getLocale();
		AsyncTask task = new AsyncTask() {
			@Override
			public void run0() {
				List<CmsContent> contents = dao().listByIds(contentIds);
				for (CmsContent c : contents) {
					LoginUser loginUser = StpAdminUtil.getLoginUser();
					PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(c.getCatalogId()), loginUser);

					offline0(c, operator, locale);
				}
				this.setProgressInfo(100, ContentCoreTips.OFFLINE_SUCCESS.locale(locale));
			}
		};
		task.setType("ContentOffline");
		asyncTaskManager.execute(task);
		return task;
	}

	@Override
	public void offline(CmsContent cmsContent, LoginUser operator) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(cmsContent.getCatalogId()), loginUser);

		offline0(cmsContent, operator, LocaleContextHolder.getLocale());
	}

	private void offline0(CmsContent cmsContent, LoginUser loginUser, Locale locale) {
		AsyncTaskManager.setTaskTenPercentProgressInfo(ContentCoreTips.OFFLINE_CONTENT.locale(locale, cmsContent.getTitle()));
		IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
		IContent<?> content = ct.loadContent(cmsContent);
		content.setOperator(Operator.of(loginUser));
		transactionTemplate.executeWithoutResult(transactionStatus -> content.offline());
		// 映射关联内容同步下线
		if (!cmsContent.isLinkContent() && !ContentCopyType.isMapping(cmsContent.getCopyType())) {
			List<CmsContent> mappingList = dao().lambdaQuery()
					.gt(CmsContent::getCopyType, ContentCopyType.Mapping)
					.eq(CmsContent::getCopyId, cmsContent.getContentId())
					.list();
			for (CmsContent c : mappingList) {
				log.debug("CC.Content[{}].offline: mapping content offline", cmsContent.getContentId());
				AsyncTaskManager.setTaskTenPercentProgressInfo(ContentCoreTips.OFFLINE_MAPPING_CONTENT.locale(locale, c.getTitle()));
				offline0(c, loginUser, locale);
			}
		}
		SpringUtils.publishEvent(new AfterContentOfflineEvent(this, content));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sort(CmsContent sortContent, CmsContent targetContent, LoginUser operator) {
		if (targetContent.getContentId().equals(sortContent.getContentId())) {
			return;
		}
		IContentType ct = ContentCoreUtils.getContentType(sortContent.getContentType());
		IContent<?> content = ct.loadContent(sortContent);
		content.setOperator(Operator.of(operator));
		content.sort(targetContent);
		SpringUtils.publishEvent(new AfterContentSortEvent(this, content));
	}

	@Override
	public void toPublish(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.dao().listByIds(contentIds);
		for (CmsContent c : contents) {
			PermissionUtils.checkPermission(CatalogPrivItem.Publish.getPermissionKey(c.getCatalogId()), operator);
			toPublish(c, operator);
		}
	}

	@Override
	public void toPublish(CmsContent cmsContent, LoginUser loginUser) {
		IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
		IContent<?> content = ct.loadContent(cmsContent);
		content.setOperator(Operator.of(loginUser));

		boolean toPublished = transactionTemplate.execute(transactionStatus -> content.toPublish());
		if (toPublished) {
			SpringUtils.publishEvent(new AfterContentToPublishEvent(this, content));
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void archive(List<Long> contentIds, LoginUser operator) {

	}

	@Override
	public boolean checkSameTitle(Long siteId, Long catalogId, Long contentId, String title) {
		CmsSite site = this.siteService.getSite(siteId);

		String repeatTitleCheckType = RepeatTitleCheckProperty.getValue(site.getConfigProps());

		if (StringUtils.isNotEmpty(repeatTitleCheckType)) {
			if (RepeatTitleCheckProperty.CheckType_Site.equals(repeatTitleCheckType)) {
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getSiteId, siteId).eq(CmsContent::getTitle, title)
						.ne(contentId != null && contentId > 0, CmsContent::getContentId, contentId);
				return this.dao().count(q) > 0;
			} else if (RepeatTitleCheckProperty.CheckType_Catalog.equals(repeatTitleCheckType)) {
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getCatalogId, catalogId).eq(CmsContent::getTitle, title)
						.ne(contentId != null && contentId > 0, CmsContent::getContentId, contentId);
				return this.dao().count(q) > 0;
			}
		}
		return false;
	}

	@Override
	public void deleteStaticFiles(CmsContent content) {
		if (content.isLinkContent()) {
			return;
		}
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		String path = content.getStaticPath();
		if (StringUtils.isEmpty(path)) {
			path = catalog.getPath() + content.getContentId() + StringUtils.DOT;
		}

		CmsSite site = this.siteService.getSite(content.getSiteId());
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		for (CmsPublishPipe publishPipe : publishPipes) {
			String siteRoot = SiteUtils.getSiteRoot(site, publishPipe.getCode());
			String filePath = siteRoot + path + site.getStaticSuffix(publishPipe.getCode());
			File file = new File(filePath);
			if (file.exists()) {
                try {
                    FileUtils.delete(file);
                } catch (IOException e) {
                    logger.error("Delete file failed: {}", filePath, e);
                }
            }
		}
	}
}
