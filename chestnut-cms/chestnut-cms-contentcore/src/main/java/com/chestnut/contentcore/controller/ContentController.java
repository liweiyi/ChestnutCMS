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
package com.chestnut.contentcore.controller;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.*;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.domain.vo.ListContentVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.listener.event.AfterContentEditorInitEvent;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.chestnut.contentcore.properties.ShortTitleLabelProperty;
import com.chestnut.contentcore.properties.SubTitleLabelProperty;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.user.preference.IncludeChildContentPreference;
import com.chestnut.contentcore.user.preference.ShowContentSubTitlePreference;
import com.chestnut.contentcore.util.*;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 内容管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.CONTENT.MODULE}")
@Priv(type = AdminUserType.TYPE, value = CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content")
public class ContentController extends CmsRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final IPublishService publishService;

	private final IResourceService resourceService;

	private final IPublishPipeService publishPipeService;

	private final ApplicationContext applicationContext;

	@XComment("{API.DOC.CMS.CONTENT.GET_LIST}")
	@GetMapping("/list")
	public R<TableData<ListContentVO>> listData(
			@RequestParam(required = false) @XComment("{API.DOC.CMS.CONTENT.CATALOG_ID}") Long catalogId,
		 	@RequestParam(required = false) @XComment("{API.DOC.CMS.CONTENT.TITLE}")  String title,
		 	@RequestParam(required = false) @XComment("{API.DOC.CMS.CONTENT.CONTENT_TYPE}")  String contentType,
		 	@RequestParam(required = false) @XComment("{API.DOC.CMS.CONTENT.STATUS}")  String status,
		 	@RequestParam(required = false) @XComment("{API.DOC.CMS.CONTENT.CREATE_TIME_BEGIN}")  LocalDateTime beginTime,
		 	@RequestParam(required = false) @XComment("{API.DOC.CMS.CONTENT.CREATE_TIME_END}")  LocalDateTime endTime
	) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		if (!IdUtils.validate(catalogId)
				|| !loginUser.hasPermission(CatalogPrivItem.View.getPermissionKey(catalogId))) {
			return this.bindDataTable(List.of());
		}
		PageRequest pr = getPageRequest();
		CmsSite site = this.getCurrentSite();
		boolean includeChild = IncludeChildContentPreference.getValue(StpAdminUtil.getLoginUser());

		LambdaQueryChainWrapper<CmsContent> q = this.contentService.dao().lambdaQuery()
				.eq(CmsContent::getSiteId, site.getSiteId())
				.eq(StringUtils.isNotEmpty(contentType), CmsContent::getContentType, contentType)
				.like(StringUtils.isNotEmpty(title), CmsContent::getTitle, title)
				.eq(StringUtils.isNotEmpty(status), CmsContent::getStatus, status)
				.ge(Objects.nonNull(beginTime), CmsContent::getCreateTime, beginTime)
				.le(Objects.nonNull(endTime), CmsContent::getCreateTime, endTime);
		if (includeChild) {
			CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
			q.like(CmsContent::getCatalogAncestors, catalog.getAncestors());
		} else {
			q.eq(CmsContent::getCatalogId, catalogId);
		}
		if (!pr.getSorts().isEmpty()) {
			pr.getSorts().forEach(order -> {
				SFunction<CmsContent, ?> sfunc = CmsContent.getSFunction(order.getColumn());
				if (Objects.nonNull(sfunc)) {
					q.orderBy(true, order.getDirection() == Direction.ASC, sfunc);
				}
			});
		} else {
			q.orderByDesc(CmsContent::getTopFlag).orderByDesc(CmsContent::getSortFlag);
		}
		Page<CmsContent> page = q.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<ListContentVO> list = page.getRecords().stream().map(ListContentVO::newInstance).toList();

		list.forEach(vo -> {
            resourceService.dealDefaultThumbnail(site, vo.getImages(), thumbnails -> {
                vo.setImagesSrc(thumbnails);
                vo.setLogoSrc(thumbnails.get(0));
            });
        });
		ContentUtils.dealCopyInfo(list, this.siteService, this.catalogService, this.contentService);
		return this.bindDataTable(list, (int) page.getTotal());
	}

	@XComment("{API.DOC.CMS.CONTENT.INIT_EDITOR}")
	@GetMapping("/init/{catalogId}/{contentType}/{contentId}")
	public R<ContentVO> initContentEditor(
			@PathVariable("catalogId") @LongId @XComment("{API.DOC.CMS.CONTENT.CATALOG_ID}") Long catalogId,
			@PathVariable("contentType") @XComment("{API.DOC.CMS.CONTENT.CONTENT_TYPE}") String contentType,
			@PathVariable("contentId") @XComment("{API.DOC.CMS.CONTENT.CONTENT_ID}") Long contentId
	) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		IContentType ct = ContentCoreUtils.getContentType(contentType);
		// 获取初始化数据
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		CmsContent contentEntity = null;
		if (IdUtils.validate(contentId)) {
			contentEntity = this.contentService.dao().getById(contentId);
			Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
			Assert.isTrue(contentEntity.getContentType().equals(contentType), ContentCoreErrorCode.DTO_NOT_MATCH_CONTENT_TYPE::exception);
			if (!catalog.getCatalogId().equals(contentEntity.getCatalogId())) {
				catalog = this.catalogService.getCatalog(catalogId); // 纠正catalogId
			}
			// 校验用户栏目权限
			PermissionUtils.checkPermission(CatalogPermissionType.CatalogPrivItem.View.getPermissionKey(contentEntity.getCatalogId()), loginUser);
		} else {
			// 校验用户栏目权限
			PermissionUtils.checkPermission(CatalogPermissionType.CatalogPrivItem.View.getPermissionKey(catalogId), loginUser);
		}
		ContentVO vo = ct.initEditor(catalog, contentEntity);
		vo.setShowSubTitle(ShowContentSubTitlePreference.getValue(loginUser));
		CmsSite site = siteService.getSite(catalog.getSiteId());
		String shortTitleLabel = ShortTitleLabelProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		String subTitleLabel = SubTitleLabelProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		vo.setShortTitleLabel(shortTitleLabel);
		vo.setSubTitleLabel(subTitleLabel);
		vo.setCatalogName(catalog.getName());
		if (Objects.nonNull(contentEntity)) {
			// 内容引导图缩略图处理
			resourceService.dealDefaultThumbnail(site, vo.getImages(), thumbnails -> {
				vo.setImagesSrc(thumbnails);
				vo.setLogoSrc(thumbnails.get(0));
			});
			// 扩展属性
			Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(contentEntity.getConfigProps(), IProperty.UseType.Content);
			vo.setConfigProps(configProps);
			// 发布通道数据
			List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					IPublishPipeProp.PublishPipePropUseType.Content, contentEntity.getPublishPipeProps());
			vo.setPublishPipeProps(publishPipeProps);
		} else {
			vo.setContentId(IdUtils.getSnowflakeId());
			vo.setCatalogId(catalog.getCatalogId());
			vo.setContentType(ct.getId());
			// 扩展属性
			Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(Map.of(), IProperty.UseType.Content);
			vo.setConfigProps(configProps);
			// 发布通道初始数据
			List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
			vo.setPublishPipe(publishPipes.stream().map(CmsPublishPipe::getCode).toArray(String[]::new));
			// 发布通道模板数据
			List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
					IPublishPipeProp.PublishPipePropUseType.Content, null);
			vo.setPublishPipeProps(publishPipeProps);
		}
		// 事件扩展
		this.applicationContext.publishEvent(new AfterContentEditorInitEvent(this, vo));
		return R.ok(vo);
	}

	@XComment("{API.DOC.CMS.CONTENT.ADD}")
	@Log(title = "CreateContent", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Map<String, Object>> addContent(@RequestParam("contentType") @XComment("{API.DOC.CMS.CONTENT.CONTENT_TYPE}") String contentType, HttpServletRequest request)
			throws IOException {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		IContentType ct = ContentCoreUtils.getContentType(contentType);

		ContentDTO dto = ct.parseRequest(request.getInputStream());
		PermissionUtils.checkPermission(CatalogPrivItem.AddContent.getPermissionKey(dto.getCatalogId()), loginUser);

		IContent<?> content = ct.dto2content(dto);
		content.setOperator(Operator.of(loginUser));

		AsyncTask task = this.contentService.addContent(content);
		return R.ok(Map.of("taskId", task.getTaskId()));
	}

	@XComment("{API.DOC.CMS.CONTENT.UPDATE}")
	@Log(title = "UpdateContent", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Map<String, Object>> saveContent(@RequestParam("contentId") @XComment("{API.DOC.CMS.CONTENT.CONTENT_TYPE}") @LongId Long contentId, HttpServletRequest request)
			throws IOException {
		LoginUser loginUser = StpAdminUtil.getLoginUser();

		CmsContent cmsContent = contentService.dao().getById(contentId);
		IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());

		ContentDTO dto = ct.parseRequest(request.getInputStream());
		this.contentService.checkSaveContentPermission(cmsContent.getCatalogId(), dto.getCatalogId(), loginUser);

		AsyncTask task = this.contentService.saveContent(dto, loginUser);
		return R.ok(Map.of("taskId", task.getTaskId()));
	}

	@XComment("{API.DOC.CMS.CONTENT.DELETE}")
	@Log(title = "DeleteContent", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<String> deleteContent(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}") List<Long> contentIds) {
		AsyncTask task = this.contentService.deleteContents(contentIds, StpAdminUtil.getLoginUser());
		return R.ok(task.getTaskId());
	}

	@XComment("{API.DOC.CMS.CONTENT.PUBLISH}")
	@Log(title = "PublishContent", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<String> publish(@RequestBody @Validated PublishContentDTO publishContentDTO) throws TemplateException, IOException {
		List<CmsContent> list = this.contentService.dao().listByIds(publishContentDTO.getContentIds());
		for (CmsContent content : list) {
			LoginUser loginUser = StpAdminUtil.getLoginUser();
			PermissionUtils.checkPermission(CatalogPrivItem.Publish.getPermissionKey(content.getCatalogId()), loginUser);
		}
		AsyncTask task = this.publishService.publishContents(list, StpAdminUtil.getLoginUser());
		return R.ok(task.getTaskId());
	}

	@XComment("{API.DOC.CMS.CONTENT.LOCK}")
	@Log(title = "LockContent", businessType = BusinessType.UPDATE)
	@PostMapping("/lock/{contentId}")
	public R<String> lock(@PathVariable("contentId") @LongId @XComment("{API.DOC.CMS.CONTENT.CONTENT_ID}") Long contentId) {
		CmsContent content = contentService.dao().getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

		LoginUser loginUser = StpAdminUtil.getLoginUser();
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(content.getCatalogId()), loginUser);

		this.contentService.lock(content, StpAdminUtil.getLoginUser().getUsername());
		return R.ok(StpAdminUtil.getLoginUser().getUsername());
	}

	@XComment("{API.DOC.CMS.CONTENT.UNLOCK}")
	@Log(title = "UnlockContent", businessType = BusinessType.UPDATE)
	@PostMapping("/unlock/{contentId}")
	public R<Void> unLock(@PathVariable("contentId") @LongId @XComment("{API.DOC.CMS.CONTENT.CONTENT_ID}") Long contentId) {
		CmsContent content = contentService.dao().getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

		LoginUser loginUser = StpAdminUtil.getLoginUser();
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(content.getCatalogId()), loginUser);

		this.contentService.unLock(content, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.COPY}")
	@Log(title = "CopyContent", businessType = BusinessType.UPDATE)
	@PostMapping("/copy")
	public R<String> copy(@RequestBody @Validated CopyContentDTO dto) {
		dto.setSourceSiteId(this.getCurrentSite().getSiteId());
		AsyncTask task = this.contentService.copy(dto);
		return R.ok(task.getTaskId());
	}

	@XComment("{API.DOC.CMS.CONTENT.MOVE}")
	@Log(title = "MoveContent", businessType = BusinessType.UPDATE)
	@PostMapping("/move")
	public R<String> move(@RequestBody @Validated MoveContentDTO dto) {
		AsyncTask task = this.contentService.move(dto);
		return R.ok(task.getTaskId());
	}

	@XComment("{API.DOC.CMS.CONTENT.SET_TOP}")
	@Log(title = "SetTopContent", businessType = BusinessType.UPDATE)
	@PostMapping("/set_top")
	public R<Void> setTop(@RequestBody @Validated SetTopContentDTO dto) {
		this.contentService.setTop(dto);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.CANCEL_TOP}")
	@Log(title = "CancelTopContent", businessType = BusinessType.UPDATE)
	@PostMapping("/cancel_top")
	public R<Void> cancelTop(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}") List<Long> contentIds) {
		this.contentService.cancelTop(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.SORT}")
	@Log(title = "SortContent", businessType = BusinessType.UPDATE)
	@PostMapping("/sort")
	public R<Void> sort(@RequestBody @Validated SortContentDTO dto) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		CmsContent sortContent = this.contentService.dao().getById(dto.getContentId());
		Assert.notNull(sortContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dto.getContentId()));
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(sortContent.getCatalogId()), loginUser);

		CmsContent targetContent = this.contentService.dao().getById(dto.getTargetContentId());
		Assert.notNull(targetContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dto.getTargetContentId()));
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(targetContent.getCatalogId()), loginUser);

		this.contentService.sort(sortContent, targetContent, loginUser);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.OFFLINE}")
	@Log(title = "OfflineContent", businessType = BusinessType.UPDATE)
	@PostMapping("/offline")
	public R<String> offline(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}") List<Long> contentIds) {
		AsyncTask task = this.contentService.offline(contentIds, StpAdminUtil.getLoginUser());
		return R.ok(task.getTaskId());
	}

	@XComment("{API.DOC.CMS.CONTENT.TO_PUBLISH}")
	@Log(title = "ToPublishContent", businessType = BusinessType.UPDATE)
	@PostMapping("/to_publish")
	public R<Void> toPublish(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}") List<Long> contentIds) {
		this.contentService.toPublish(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.ARCHIVE}")
	@Log(title = "ArchiveContent", businessType = BusinessType.UPDATE)
	@PostMapping("/archive")
	public R<Void> archive(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}") List<Long> contentIds) {
		this.contentService.archive(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.ADD_ATTR}")
	@Log(title = "AddContentAttr", businessType = BusinessType.UPDATE)
	@PostMapping("/attr")
	public R<Void> addContentsAttribute(@RequestBody @Validated ChangeContentAttrDTO dto) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		List<CmsContent> contents = this.contentService.dao().listByIds(dto.getContentIds());
        for (CmsContent c : contents) {
			PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(c.getCatalogId()), loginUser);

            int attributes = ContentAttribute.append(c.getAttributes(), ContentAttribute.bit(dto.getAttr()));
            this.contentService.dao().lambdaUpdate().set(CmsContent::getAttributes, attributes).eq(CmsContent::getContentId, c.getContentId()).update();
        }
        return R.ok();
	}

	@XComment("{API.DOC.CMS.CONTENT.REMOVE_ATTR}")
	@Log(title = "RemoveContentAttr", businessType = BusinessType.UPDATE)
	@PostMapping("/attr/delete")
	public R<Void> removeContentsAttribute(@RequestBody @Validated ChangeContentAttrDTO dto) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		List<CmsContent> contents = this.contentService.dao().listByIds(dto.getContentIds());
		for (CmsContent c : contents) {
			PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(c.getCatalogId()), loginUser);

			int attributes = ContentAttribute.remove(c.getAttributes(), ContentAttribute.bit(dto.getAttr()));
			this.contentService.dao().lambdaUpdate().set(CmsContent::getAttributes, attributes).eq(CmsContent::getContentId, c.getContentId()).update();
		}
		return R.ok();
	}
}
