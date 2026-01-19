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

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PageWidgetAddDTO;
import com.chestnut.contentcore.domain.dto.PageWidgetEditDTO;
import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import freemarker.template.TemplateException;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 页面部件控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/pagewidget")
public class PageWidgetController extends CmsRestController {

	private final ICatalogService catalogService;

	private final IPageWidgetService pageWidgetService;

	private final IPublishPipeService publishPipeService;

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/types")
	public R<?> getPageWidgetTypes() {
		List<Map<String, String>> result = this.pageWidgetService.getPageWidgetTypes().stream().map(bt -> {
			return Map.of("id", bt.getId(), "name", I18nUtils.get(bt.getName()));
		}).toList();
		return this.bindDataTable(result);
	}

	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.ContentView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER },
			mode = SaMode.AND
	)
	@GetMapping("/list")
	public R<?> listData(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "type", required = false) String type,
			@RequestParam(name = "state", required = false) Integer state) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.getCurrentSite();
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, site.getSiteId())
//				.eq(catalogId != null && catalogId > 0, CmsPageWidget::getCatalogId, catalogId)
				.like(StringUtils.isNotEmpty(name), CmsPageWidget::getName, name)
				.eq(StringUtils.isNotEmpty(type), CmsPageWidget::getType, type)
				.eq(state != null && state > -1, CmsPageWidget::getState, state)
				.orderByDesc(CmsPageWidget::getCreateTime);
		Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		List<PageWidgetVO> list = new ArrayList<>();
		page.getRecords().forEach(pw -> {
			PageWidgetVO vo = PageWidgetVO.newInstance(pw);
			if (pw.getCatalogId() > 0) {
				CmsCatalog catalog = catalogService.getCatalog(pw.getCatalogId());
				vo.setCatalogName(catalog.getName());
			}
			IPageWidgetType pageWidgetType = this.pageWidgetService.getPageWidgetType(vo.getType());
			vo.setRoute(pageWidgetType.getRoute());
			list.add(vo);
		});
		return this.bindDataTable(list, (int) page.getTotal());
	}

	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.ContentView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER },
			mode = SaMode.AND
	)
	@GetMapping("/detail/{pageWidgetId}")
	public R<PageWidgetVO> getInfo(@PathVariable("pageWidgetId") Long pageWidgetId) {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(pageWidgetId);
		Assert.notNull(pageWidget, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageWidgetId", pageWidgetId));

		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pageWidget.getType());
		PageWidgetVO vo = pwt.getPageWidgetVO(pageWidget);
		// 发布通道模板
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(pageWidget.getSiteId());
		List<PublishPipeTemplate> templates = publishPipes.stream().map(pp -> new PublishPipeTemplate(
						pp.getName(),
						pp.getCode(),
						pageWidget.getTemplate(pp.getCode())
				)).toList();
		vo.setTemplates(templates);
		if (pageWidget.getCatalogId() > 0) {
			CmsCatalog catalog = this.catalogService.getCatalog(pageWidget.getCatalogId());
			vo.setCatalogName(catalog.getName());
		}
		return R.ok(vo);
	}

	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.ContentView, SitePermissionType.ID + ":AddPageWidget:${#_header['" + ContentCoreConsts.Header_CurrentSite + "']}" },
			mode = SaMode.AND
	)
	@Log(title = "新增页面组件", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<?> addPageWidget(@RequestBody @Validated PageWidgetAddDTO dto) {
		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(dto.getType());
		Assert.notNull(pwt, () -> ContentCoreErrorCode.UNSUPPORTED_PAGE_WIDGET_TYPE.exception(dto.getType()));

		CmsSite site = getCurrentSite();
		CmsPageWidget pageWidget= new CmsPageWidget();
		BeanUtils.copyProperties(dto, pageWidget);
		pageWidget.setSiteId(site.getSiteId());
		pageWidget.setTemplates(dto.getPublishPipeTemplateMap());
		
		IPageWidget pw = pwt.newInstance();
		pw.setPageWidgetEntity(pageWidget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		if (IdUtils.validate(pageWidget.getCatalogId())) {
			CmsCatalog catalog = this.catalogService.getCatalog(pageWidget.getCatalogId());
			pageWidget.setCatalogAncestors(catalog.getAncestors());
		}
		this.pageWidgetService.addPageWidget(pw);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "编辑页面组件", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<?> editPageWidget(@RequestBody @Validated PageWidgetEditDTO dto) {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(dto.getPageWidgetId());
		Assert.notNull(dto.getPageWidgetId(), () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageWidgetId", dto.getPageWidgetId()));
		
		BeanUtils.copyProperties(dto, pageWidget);
		pageWidget.setContent(dto.getContentStr());
		pageWidget.setTemplates(dto.getPublishPipeTemplateMap());

		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pageWidget.getType());
		IPageWidget pw = pwt.newInstance();
		pw.setPageWidgetEntity(pageWidget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		this.pageWidgetService.savePageWidget(pw);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "删除页面组件", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> deletePageWidgets(@RequestBody @NotEmpty List<Long> pageWidgetIds) {
		this.pageWidgetService.deletePageWidgets(pageWidgetIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "页面组件", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<?> publishPageWidgets(@RequestBody @NotEmpty List<Long> pageWidgetIds) throws TemplateException, IOException {
		this.pageWidgetService.publishPageWidgets(pageWidgetIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

    @Priv(type = AdminUserType.TYPE)
    @Log(title = "页面组件", businessType = BusinessType.OTHER)
    @PostMapping("/offline")
    public R<?> offlinePageWidgets(@RequestBody @NotEmpty List<Long> pageWidgetIds) throws TemplateException, IOException {
        this.pageWidgetService.offlinePageWidgets(pageWidgetIds, StpAdminUtil.getLoginUser());
        return R.ok();
    }
}
