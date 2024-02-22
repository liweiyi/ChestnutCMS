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
package com.chestnut.xmodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.dto.XModelDTO;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
		List<Map<String, String>> list = controlTypes.stream().map(t ->
			Map.of("id", t.getType(), "name", I18nUtils.get(t.getName()))
		).toList();
		return R.ok(list);
	}

	@GetMapping
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<XModel> page = this.modelService.lambdaQuery().like(StringUtils.isNotEmpty(query), XModel::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/tables")
	public R<?> getModelDataTableList(@RequestParam String type) {
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

	@GetMapping("/{modelId}")
	public R<?> getModel(@PathVariable @LongId Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		model.setIsDefaultTable(mmt.getDefaultTable().equals(model.getTableName()));
		return R.ok(model);
	}

	@Log(title = "新增元数据", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@RequestBody @Validated XModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelService.addModel(dto);
		return R.ok();
	}

	@Log(title = "编辑元数据", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@RequestBody @Validated XModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelService.editModel(dto);
		return R.ok();
	}

	@Log(title = "删除元数据", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<XModelDTO> dtoList) {
		List<Long> modelIds = dtoList.stream().map(XModelDTO::getModelId).toList();
		this.modelService.deleteModel(modelIds);
		return R.ok();
	}
}
