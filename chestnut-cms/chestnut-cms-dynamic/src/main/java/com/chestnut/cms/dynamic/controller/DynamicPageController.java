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
package com.chestnut.cms.dynamic.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.dynamic.core.IDynamicPageInitData;
import com.chestnut.cms.dynamic.domain.CmsDynamicPage;
import com.chestnut.cms.dynamic.domain.vo.DynamicPageInitDataTypeVO;
import com.chestnut.cms.dynamic.service.IDynamicPageService;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自定义动态模板页面管理控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Priv(type = AdminUserType.TYPE, value = CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/dynamic_page")
public class DynamicPageController extends BaseRestController {

	private final ISiteService siteService;

	private final IDynamicPageService dynamicPageService;

	private final List<IDynamicPageInitData> initDataTypes;

	@GetMapping("/list")
	public R<?> bindList(@RequestParam(required = false) String query) {
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		PageRequest pr = getPageRequest();
		Page<CmsDynamicPage> page = this.dynamicPageService.lambdaQuery()
				.eq(CmsDynamicPage::getSiteId, site.getSiteId())
				.and(StringUtils.isNotEmpty(query), wrapper ->
						wrapper.like(CmsDynamicPage::getCode, query)
								.or().like(CmsDynamicPage::getName, query)
								.or().like(CmsDynamicPage::getPath, query))
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		return this.bindDataTable(page);
	}

	@GetMapping("/init_data_types")
	public R<?> getInitDataTypes() {
		List<DynamicPageInitDataTypeVO> list = initDataTypes.stream()
				.map(DynamicPageInitDataTypeVO::newInstance).toList();
		return R.ok(list);
	}

	@GetMapping("/{pageId}")
	public R<?> getDynamicPageInfo(@PathVariable @LongId Long pageId) {
		CmsDynamicPage dynamicPage = this.dynamicPageService.getById(pageId);
		Assert.notNull(dynamicPage, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageId", pageId));
		return R.ok(dynamicPage);
	}

	@Log(title = "新增自定动态模板页面", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addSave(@RequestBody @Validated CmsDynamicPage dynamicPage) {
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		dynamicPage.setSiteId(site.getSiteId());
		dynamicPage.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.dynamicPageService.addDynamicPage(dynamicPage);
		return R.ok();
	}

	@Log(title = "编辑自定动态模板页面", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editSave(@RequestBody @Validated CmsDynamicPage dynamicPage) {
		dynamicPage.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.dynamicPageService.saveDynamicPage(dynamicPage);
		return R.ok();
	}

	@Log(title = "删除自定动态模板页面", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<String> remove(@RequestBody @NotEmpty List<Long> publishPipeIds) {
		this.dynamicPageService.deleteDynamicPage(publishPipeIds);
		return R.ok();
	}
}
