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
package com.chestnut.advertisement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.advertisement.AdSpacePageWidgetType;
import com.chestnut.advertisement.pojo.vo.AdSpaceVO;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PageWidgetAddDTO;
import com.chestnut.contentcore.domain.dto.PageWidgetEditDTO;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 广告位页面部件管理前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/adspace")
public class AdSpaceController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPageWidgetService pageWidgetService;

	private final AdSpacePageWidgetType pageWidgetType;

	@GetMapping
	public R<?> listAdSpaces(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "state", required = false) Integer state) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, site.getSiteId())
				.eq(catalogId != null && catalogId > 0, CmsPageWidget::getCatalogId, catalogId)
				.like(StringUtils.isNotEmpty(name), CmsPageWidget::getName, name)
				.eq(CmsPageWidget::getType, AdSpacePageWidgetType.ID)
				.eq(state != null && state > -1, CmsPageWidget::getState, state)
				.orderByDesc(CmsPageWidget::getCreateTime);
		Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		List<AdSpaceVO> list = new ArrayList<>();
		page.getRecords().forEach(pw -> {
			AdSpaceVO vo = (AdSpaceVO) pageWidgetType.getPageWidgetVO(pw);
			if (pw.getCatalogId() > 0) {
				CmsCatalog catalog = catalogService.getCatalog(pw.getCatalogId());
				vo.setCatalogName(catalog != null ? catalog.getName() : "未知");
			}
			list.add(vo);
		});
		return this.bindDataTable(list, (int) page.getTotal());
	}

	@GetMapping("/{adSpaceId}")
	public R<PageWidgetVO> getAdSpaceInfo(@PathVariable("adSpaceId") Long adSpaceId) {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(adSpaceId);
		if (pageWidget == null) {
			return R.fail("数据未找到：" + adSpaceId);
		}
		AdSpaceVO vo = (AdSpaceVO) pageWidgetType.getPageWidgetVO(pageWidget);
		CmsCatalog catalog = this.catalogService.getCatalog(pageWidget.getCatalogId());
		vo.setCatalogName(catalog != null ? catalog.getName() : "未知");
		return R.ok(vo);
	}

	@PostMapping
	public R<?> addAdSpace(HttpServletRequest request) throws IOException {
		PageWidgetAddDTO dto = JacksonUtils.from(request.getInputStream(), PageWidgetAddDTO.class);
		dto.setType(pageWidgetType.getId());
		
		CmsPageWidget cmsPageWdiget = new CmsPageWidget();
		BeanUtils.copyProperties(dto, cmsPageWdiget);
		IPageWidget pw = pageWidgetType.newInstance();
		pw.setPageWidgetEntity(cmsPageWdiget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		CmsSite site = this.siteService.getCurrentSite(request);
		pw.getPageWidgetEntity().setSiteId(site.getSiteId());
		this.pageWidgetService.addPageWidget(pw);
		return R.ok();
	}

	@PutMapping
	public R<?> editAdSpace(HttpServletRequest request) throws IOException {
		PageWidgetEditDTO dto = JacksonUtils.from(request.getInputStream(), PageWidgetEditDTO.class);
		CmsPageWidget cmsPageWdiget = new CmsPageWidget();
		BeanUtils.copyProperties(dto, cmsPageWdiget);
		IPageWidget pw = pageWidgetType.newInstance();
		pw.setPageWidgetEntity(cmsPageWdiget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		this.pageWidgetService.savePageWidget(pw);
		return R.ok();
	}

	@DeleteMapping
	public R<?> deleteAdSpaces(@RequestBody List<Long> adSpaceIds) {
		Assert.notEmpty(adSpaceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("adSpaceIds"));
		this.pageWidgetService.deletePageWidgets(adSpaceIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@PostMapping("/publish")
	public R<?> publishPageWidgets(@RequestBody List<Long> adSpaceIds) throws TemplateException, IOException {
		Assert.notEmpty(adSpaceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("adSpaceIds"));
		this.pageWidgetService.publishPageWidgets(adSpaceIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}
}
