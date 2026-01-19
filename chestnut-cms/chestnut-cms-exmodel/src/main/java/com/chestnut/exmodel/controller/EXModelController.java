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
package com.chestnut.exmodel.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.exmodel.CmsExtendMetaModelType;
import com.chestnut.exmodel.permission.EXModelPriv;
import com.chestnut.exmodel.service.ExModelService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.dto.CreateXModelRequest;
import com.chestnut.xmodel.dto.UpdateXModelRequest;
import com.chestnut.xmodel.dto.XModelFieldDataDTO;
import com.chestnut.xmodel.service.IModelService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 扩展模型前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/exmodel")
@RequiredArgsConstructor
public class EXModelController extends CmsRestController {

	private final IModelService modelService;

	private final ISiteService siteService;

	private final ExModelService extendModelService;

    @Priv(
            type = AdminUserType.TYPE,
            value = { EXModelPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@GetMapping("/list")
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.getCurrentSite();
		LambdaQueryWrapper<XModel> q = new LambdaQueryWrapper<XModel>()
				.eq(XModel::getOwnerType, CmsExtendMetaModelType.TYPE)
				.eq(XModel::getOwnerId, site.getSiteId().toString())
				.like(StringUtils.isNotEmpty(query), XModel::getName, query);
		Page<XModel> page = this.modelService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

    @Priv(type = AdminUserType.TYPE, value = { CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER })
	@GetMapping("/options")
	public R<?> getModelOptions(@RequestParam(required = false) Long siteId) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.getCurrentSite();
		if (IdUtils.validate(siteId)) {
			site = this.siteService.getSite(siteId);
		}
		LambdaQueryWrapper<XModel> q = new LambdaQueryWrapper<XModel>()
				.eq(XModel::getOwnerType, CmsExtendMetaModelType.TYPE)
				.eq(XModel::getOwnerId, site.getSiteId().toString());
		Page<XModel> page = this.modelService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增扩展模型", businessType = BusinessType.INSERT)
    @Priv(
            type = AdminUserType.TYPE,
            value = { EXModelPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated CreateXModelRequest req) {
		CmsSite site = this.getCurrentSite();
		req.setOwnerType(CmsExtendMetaModelType.TYPE);
		req.setOwnerId(site.getSiteId().toString());
		this.modelService.addModel(req);
		return R.ok();
	}

	@Log(title = "编辑扩展模板", businessType = BusinessType.UPDATE)
    @Priv(
            type = AdminUserType.TYPE,
            value = { EXModelPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@PostMapping("/update")
	public R<?> edit(@RequestBody @Validated UpdateXModelRequest req) {
		this.modelService.editModel(req);
		return R.ok();
	}

	@Log(title = "删除扩展模型", businessType = BusinessType.DELETE)
    @Priv(
            type = AdminUserType.TYPE,
            value = { EXModelPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@PostMapping("/delete")
	public R<?> remove(@RequestBody @Validated @NotEmpty List<Long> modelIds) {
		this.modelService.deleteModel(modelIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/data")
	public R<?> getModelData(@RequestParam @LongId Long modelId,
							 @RequestParam String dataType,
							 @RequestParam(required = false, defaultValue = "") String dataId) {
		List<XModelFieldDataDTO> data = extendModelService.getModelData(modelId, dataType, dataId);
		return R.ok(data);
	}
}
