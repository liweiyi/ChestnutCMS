/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.domain.R;
import com.chestnut.common.extend.annotation.XssIgnore;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.*;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.domain.vo.ListContentVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.listener.event.AfterContentEditorInitEvent;
import com.chestnut.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.user.preference.IncludeChildContentPreference;
import com.chestnut.contentcore.user.preference.ShowContentSubTitlePreference;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 内容管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content")
public class ContentController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final IPublishService publishService;

	private final ApplicationContext applicationContext;

	/**
	 * 内容列表
	 */
	@GetMapping("/list")
	public R<?> listData(@RequestParam(required = false) Long catalogId,
						 @RequestParam(required = false) String title,
						 @RequestParam(required = false) String contentType,
						 @RequestParam(required = false) String status,
						 @RequestParam(required = false) LocalDateTime beginTime,
						 @RequestParam(required = false) LocalDateTime endTime) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		if (!IdUtils.validate(catalogId)
				|| !loginUser.hasPermission(CatalogPrivItem.View.getPermissionKey(catalogId))) {
			return this.bindDataTable(List.of());
		}
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		boolean includeChild = IncludeChildContentPreference.getValue(StpAdminUtil.getLoginUser());

		LambdaQueryChainWrapper<CmsContent> q = this.contentService.lambdaQuery()
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
		List<ListContentVO> list = new ArrayList<>();
		page.getRecords().forEach(content -> {
			ListContentVO vo = new ListContentVO();
			BeanUtils.copyProperties(content, vo);
			vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(content.getLogo()));
			vo.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
			vo.setInternalUrl(InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, content.getContentId()));
			list.add(vo);
		});
		return this.bindDataTable(list, (int) page.getTotal());
	}

	/**
	 * 内容编辑数据初始化
	 */
	@GetMapping("/init/{catalogId}/{contentType}/{contentId}")
	public R<ContentVO> initContentEditor(@PathVariable("catalogId") @LongId Long catalogId,
			@PathVariable("contentType") String contentType, @PathVariable("contentId") Long contentId) {
		IContentType ct = ContentCoreUtils.getContentType(contentType);
		// 获取初始化数据
		ContentVO vo = ct.initEditor(catalogId, contentId);
		vo.setShowSubTitle(ShowContentSubTitlePreference.getValue(StpAdminUtil.getLoginUser()));
		// 事件扩展
		this.applicationContext.publishEvent(new AfterContentEditorInitEvent(this, vo));
		return R.ok(vo);
	}

	@Log(title = "新增内容", businessType = BusinessType.INSERT)
	@XssIgnore
	@PostMapping
	public R<?> addContent(@RequestParam("contentType") String contentType, HttpServletRequest request)
			throws IOException {
		IContentType ct = ContentCoreUtils.getContentType(contentType);

		IContent<?> content = ct.readRequest(request);
		content.setOperator(StpAdminUtil.getLoginUser());
		PermissionUtils.checkPermission(CatalogPrivItem.AddContent.getPermissionKey(content.getCatalogId()),
				content.getOperator());

		AsyncTask task = this.contentService.addContent(content);
		return R.ok(Map.of("taskId", task.getTaskId()));
	}

	@Log(title = "编辑内容", businessType = BusinessType.UPDATE)
	@XssIgnore
	@PutMapping
	public R<?> saveContent(@RequestParam("contentType") String contentType, HttpServletRequest request)
			throws IOException {
		IContentType ct = ContentCoreUtils.getContentType(contentType);

		IContent<?> content = ct.readRequest(request);
		content.setOperator(StpAdminUtil.getLoginUser());
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(content.getCatalogId()),
				content.getOperator());

		AsyncTask task = this.contentService.saveContent(content);
		return R.ok(Map.of("taskId", task.getTaskId()));
	}

	@Log(title = "删除内容", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteContent(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.deleteContents(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 发布内容
	 */
	@Log(title = "发布内容", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<String> publish(@RequestBody @Validated PublishContentDTO publishContentDTO) throws TemplateException, IOException {
		this.publishService.publishContent(publishContentDTO.getContentIds(), StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 锁定内容
	 */
	@Log(title = "锁定内容", businessType = BusinessType.UPDATE)
	@PostMapping("/lock/{contentId}")
	public R<String> lock(@PathVariable("contentId") @LongId Long contentId) {
		this.contentService.lock(contentId, StpAdminUtil.getLoginUser().getUsername());
		return R.ok(StpAdminUtil.getLoginUser().getUsername());
	}

	/**
	 * 解锁内容
	 */
	@Log(title = "解锁内容", businessType = BusinessType.UPDATE)
	@PostMapping("/unlock/{contentId}")
	public R<String> unLock(@PathVariable("contentId") @LongId Long contentId) {
		this.contentService.unLock(contentId, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 复制内容
	 */
	@Log(title = "复制内容", businessType = BusinessType.UPDATE)
	@PostMapping("/copy")
	public R<?> copy(@RequestBody @Validated CopyContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.copy(dto);
		return R.ok();
	}

	/**
	 * 转移内容
	 */
	@Log(title = "转移内容", businessType = BusinessType.UPDATE)
	@PostMapping("/move")
	public R<?> move(@RequestBody @Validated MoveContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.move(dto);
		return R.ok();
	}

	/**
	 * 置顶
	 */
	@Log(title = "置顶", businessType = BusinessType.UPDATE)
	@PostMapping("/set_top")
	public R<?> setTop(@RequestBody @Validated SetTopContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.setTop(dto);
		return R.ok();
	}

	/**
	 * 取消置顶
	 */
	@Log(title = "取消置顶", businessType = BusinessType.UPDATE)
	@PostMapping("/cancel_top")
	public R<?> cancelTop(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.cancelTop(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 排序
	 */
	@Log(title = "内容排序", businessType = BusinessType.UPDATE)
	@PostMapping("/sort")
	public R<?> sort(@RequestBody @Validated SortContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.sort(dto);
		return R.ok();
	}

	/**
	 * 下线
	 */
	@Log(title = "下线内容", businessType = BusinessType.UPDATE)
	@PostMapping("/offline")
	public R<?> offline(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.offline(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 待发布
	 */
	@Log(title = "待发布内容", businessType = BusinessType.UPDATE)
	@PostMapping("/to_publish")
	public R<?> toPublish(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.toPublish(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 归档
	 */
	@Log(title = "归档内容", businessType = BusinessType.UPDATE)
	@PostMapping("/archive")
	public R<?> archive(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.archive(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@Log(title = "添加内容属性", businessType = BusinessType.UPDATE)
	@PostMapping("/attr")
	public R<?> addContentsAttribute(@RequestBody @Validated ChangeContentAttrDTO dto) {
		this.contentService.listByIds(dto.getContentIds()).forEach(content -> {
			int attributes = ContentAttribute.append(content.getAttributes(), ContentAttribute.bit(dto.getAttr()));
			this.contentService.lambdaUpdate().set(CmsContent::getAttributes, attributes).eq(CmsContent::getContentId, content.getContentId()).update();
		});
		return R.ok();
	}

	@Log(title = "移除内容属性", businessType = BusinessType.UPDATE)
	@DeleteMapping("/attr")
	public R<?> removeContentsAttribute(@RequestBody @Validated ChangeContentAttrDTO dto) {
		this.contentService.listByIds(dto.getContentIds()).forEach(content -> {
			int attributes = ContentAttribute.remove(content.getAttributes(), ContentAttribute.bit(dto.getAttr()));
			this.contentService.lambdaUpdate().set(CmsContent::getAttributes, attributes).eq(CmsContent::getContentId, content.getContentId()).update();
		});
		return R.ok();
	}
}
