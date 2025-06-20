/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.impl.CatalogType_Common;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.dao.CmsContentDAO;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.domain.dto.CopyContentDTO;
import com.chestnut.contentcore.domain.dto.MoveContentDTO;
import com.chestnut.contentcore.domain.dto.SetTopContentDTO;
import com.chestnut.contentcore.domain.dto.SortContentDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentOpType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.chestnut.contentcore.properties.RepeatTitleCheckProperty;
import com.chestnut.contentcore.publish.IContentPathRule;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.ContentLogUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.fixed.config.BackendContext;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements IContentService {

	private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final AsyncTaskManager asyncTaskManager;

	private final CmsContentDAO dao;

	@Override
	public CmsContentDAO dao() {
		return dao;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteContents(List<Long> contentIds, LoginUser operator) {
		for (Long contentId : contentIds) {
			CmsContent xContent = this.dao().getById(contentId);
			Assert.notNull(xContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
			PermissionUtils.checkPermission(CatalogPrivItem.DeleteContent.getPermissionKey(xContent.getCatalogId()), operator);

			boolean canDelete = ContentStatus.isDraft(xContent.getStatus()) || ContentStatus.isOffline(xContent.getStatus());
			Assert.isTrue(canDelete, ContentCoreErrorCode.DEL_CONTENT_ERR::exception);

			IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
			IContent<?> content = contentType.loadContent(xContent);
			content.setOperator(operator);
			content.delete();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverContents(List<Long> backupIds, LoginUser operator) {
		Map<Long, Integer> catalogContentIncr = new HashMap<>();
		List<BCmsContent> backupContents = this.dao().getBackupByIds(backupIds);
		for (BCmsContent backupContent : backupContents) {
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
			IContentType contentType = ContentCoreUtils.getContentType(backupContent.getContentType());
			contentType.deleteBackups(backupContent.getContentId());
		}
	}

	@Override
	public void deleteContentsByCatalog(CmsCatalog catalog, boolean includeChild, LoginUser operator) {
		long pageSize = 100;
		long total = this.dao().lambdaQuery()
				.eq(!includeChild, CmsContent::getCatalogId, catalog.getCatalogId())
				.likeRight(includeChild, CmsContent::getCatalogAncestors, catalog.getAncestors())
				.count();

		for (int i = 0; i * pageSize < total; i++) {
			AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize / total),
					"正在栏目删除内容：" + (i * pageSize) + " / " + total);
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
		if (catalog.isStaticize()) {
			String contentPath = content.getStaticPath();
			if (StringUtils.isEmpty(contentPath)) {
				IContentPathRule rule = ContentCoreUtils.getContentPathRule(catalog.getDetailNameRule());
				String path = Objects.isNull(rule) ? catalog.getPath() : rule.getDirectory(site, catalog, content);
				contentPath = path + content.getContentId() + "." + site.getStaticSuffix(publishPipeCode);
			}
			return site.getUrl(publishPipeCode) + contentPath;
		} else {
			String viewPath = IInternalDataType.getViewPath(InternalDataType_Content.ID, content.getContentId(),
					publishPipeCode, pageIndex);
			return site.getUrl(publishPipeCode) + viewPath;
		}
	}

	@Override
	public void lock(Long contentId, String operator) {
		CmsContent content = this.dao().getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
		boolean checkLock = content.isLock() && StringUtils.isNotEmpty(content.getLockUser())
				&& !StringUtils.equals(content.getLockUser(), operator);
		Assert.isFalse(checkLock, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getLockUser()));

		content.setIsLock(YesOrNo.YES);
		content.setLockUser(operator);
		content.updateBy(operator);
		this.dao().updateById(content);
		ContentLogUtils.addLog(ContentOpType.LOCK, content, AdminUserType.TYPE, operator);
	}

	@Override
	public void unLock(Long contentId, String operator) {
		CmsContent content = this.dao().getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
		if (!content.isLock()) {
			return;
		}
		boolean checkOp = StringUtils.isNotEmpty(content.getLockUser())
				&& !StringUtils.equals(content.getLockUser(), operator);
		Assert.isFalse(checkOp, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getLockUser()));
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
			}
		};
		task.setType("SaveContent-" + content.getContentEntity().getContentId());
		asyncTaskManager.execute(task);
		return task;
	}

	@Transactional(rollbackFor = Exception.class)
	public void addContent0(IContent<?> content) {
		content.add();
		AsyncTaskManager.setTaskPercent(100);
	}

	@Override
	public AsyncTask saveContent(IContent<?> content) {
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() {
				saveContent0(content);
			}
		};
		task.setType("SaveContent-" + content.getContentEntity().getContentId());
		asyncTaskManager.execute(task);
		return task;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveContent0(IContent<?> content) {
		content.save();
		AsyncTaskManager.setTaskPercent(100);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copy(CopyContentDTO dto) {
		List<Long> contentIds = dto.getContentIds();
		for (Long contentId : contentIds) {
			CmsContent cmsContent = this.dao().getById(contentId);
			Assert.notNull(cmsContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			for (Long catalogId : dto.getCatalogIds()) {
				CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
				if (catalog == null) {
					continue; // 目标栏目错误直接跳过
				}
				if (!catalog.getCatalogType().equals(CatalogType_Common.ID)) {
					continue; // 非普通栏目不能复制
				}
				IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
				IContent<?> content = ct.loadContent(cmsContent);
				content.setOperator(dto.getOperator());
				content.copyTo(catalog, dto.getCopyType());
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void move(MoveContentDTO dto) {
		List<Long> contentIds = dto.getContentIds();
		for (Long contentId : contentIds) {
			CmsContent cmsContent = this.dao().getById(contentId);
			Assert.notNull(cmsContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
			Assert.notNull(catalog,
					() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));

			moveContent(cmsContent, catalog, dto.getOperator());
		}
	}

	@Override
	public void moveContent(CmsContent cmsContent, CmsCatalog toCatalog, LoginUser operator) {
		if (cmsContent.getCatalogId().equals(toCatalog.getCatalogId())) {
			log.warn("Cannot move content to source catalog!");
			return;
		}
		if (!toCatalog.getCatalogType().equals(CatalogType_Common.ID)) {
			log.warn("Cannot move content to catalog which type is not common!");
			return;
		}
		IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
		IContent<?> content = ct.loadContent(cmsContent);
		content.setOperator(operator);
		content.moveTo(toCatalog);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setTop(SetTopContentDTO dto) {
		List<CmsContent> contents = this.dao().listByIds(dto.getContentIds());
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(dto.getOperator());
			content.setTop(dto.getTopEndTime());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelTop(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.dao().listByIds(contentIds);
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(operator);
			content.cancelTop();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void offline(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.dao().listByIds(contentIds);
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(operator);
			content.offline();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sort(SortContentDTO dto) {
		CmsContent c = this.dao().getById(dto.getContentId());
		IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
		IContent<?> content = ct.loadContent(c);
		content.setOperator(dto.getOperator());
		content.sort(dto.getTargetContentId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void toPublish(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.dao().listByIds(contentIds);
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(operator);
			content.toPublish();
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
