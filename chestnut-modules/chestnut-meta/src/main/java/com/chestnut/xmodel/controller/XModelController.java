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
package com.chestnut.xmodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.dto.CreateXModelRequest;
import com.chestnut.xmodel.dto.UpdateXModelRequest;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 元数据模型前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/xmodel")
public class XModelController extends BaseRestController {

	private final IModelService modelService;

	private final List<IMetaControlType> controlTypes;

	@GetMapping("/controls")
	public R<?> getControlTypeOptions() {
		return bindSelectOptions(controlTypes, IMetaControlType::getType, IMetaControlType::getName);
	}

	@GetMapping("/list")
	public R<?> getModelList(@RequestParam(required = false) @Length(max = 100) String query) {
		PageRequest pr = this.getPageRequest();
		Page<XModel> page = this.modelService.lambdaQuery().like(StringUtils.isNotEmpty(query), XModel::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/tables")
	public R<?> getModelDataTableList(@RequestParam @Length(max = 30) String type) {
		List<String> list = this.modelService.listModelDataTables(type);
		return this.bindDataTable(list);
	}

	@GetMapping("/tableFields")
	public R<?> getModelTableFields(@RequestParam("modelId") @LongId Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		List<String> list = this.modelService.listModelTableFields(model);
		return this.bindDataTable(list);
	}

	@GetMapping("/detail/{modelId}")
	public R<?> getModel(@PathVariable @LongId Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		model.setIsDefaultTable(mmt.getDefaultTable().equals(model.getTableName()));
		return R.ok(model);
	}

	@Log(title = "新增元数据", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated CreateXModelRequest req) {
		this.modelService.addModel(req);
		return R.ok();
	}

	@Log(title = "编辑元数据", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<?> edit(@RequestBody @Validated UpdateXModelRequest req) {
		this.modelService.editModel(req);
		return R.ok();
	}

	@Log(title = "删除元数据", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> remove(@RequestBody @NotEmpty List<Long> modelIds) {
		this.modelService.deleteModel(modelIds);
		return R.ok();
	}
}
