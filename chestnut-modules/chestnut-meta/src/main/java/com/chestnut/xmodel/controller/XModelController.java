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
package com.chestnut.xmodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
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
@XComment("{API.DOC.META.MODEL_MODULE}")
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/xmodel")
public class XModelController extends BaseRestController {

	private final IModelService modelService;

	private final List<IMetaControlType> controlTypes;

	@XComment("{API.DOC.META.GET_CONTROL_TYPES}")
	@GetMapping("/controls")
	public R<?> getControlTypeOptions() {
		return bindSelectOptions(controlTypes, IMetaControlType::getType, IMetaControlType::getName);
	}

	@XComment("{API.DOC.META.MODEL_GET_LIST}")
	@GetMapping("/list")
	public R<TableData<XModel>> getModelList(@RequestParam(required = false) @Length(max = 100) @XComment("{CC.ENTITY.QUERY}") String query) {
		PageRequest pr = this.getPageRequest();
		Page<XModel> page = this.modelService.lambdaQuery().like(StringUtils.isNotEmpty(query), XModel::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.META.MODEL_GET_TABLES}")
	@GetMapping("/tables")
	public R<?> getModelDataTableList(@RequestParam @Length(max = 30) @XComment("{API.DOC.META.OWNER_TYPE}") String type) {
		List<String> list = this.modelService.listModelDataTables(type);
		return this.bindDataTable(list);
	}

	@XComment("{API.DOC.META.MODEL_GET_TABLE_FIELDS}")
	@GetMapping("/tableFields")
	public R<?> getModelTableFields(@RequestParam("modelId") @LongId @XComment("{API.DOC.META.MODEL_ID}") Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		List<String> list = this.modelService.listModelTableFields(model);
		return this.bindDataTable(list);
	}

	@XComment("{API.DOC.META.MODEL_GET_DETAIL}")
	@GetMapping("/detail/{modelId}")
	public R<XModel> getModel(@PathVariable @LongId @XComment("{API.DOC.META.MODEL_ID}") Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		model.setIsDefaultTable(mmt.getDefaultTable().equals(model.getTableName()));
		return R.ok(model);
	}

	@XComment("{API.DOC.META.MODEL_ADD}")
	@Log(title = "新增元数据", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@RequestBody @Validated CreateXModelRequest req) {
		this.modelService.addModel(req);
		return R.ok();
	}

	@XComment("{API.DOC.META.MODEL_UPDATE}")
	@Log(title = "编辑元数据", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> edit(@RequestBody @Validated UpdateXModelRequest req) {
		this.modelService.editModel(req);
		return R.ok();
	}

	@XComment("{API.DOC.META.MODEL_DELETE}")
	@Log(title = "删除元数据", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.META.MODEL_IDS}") List<Long> modelIds) {
		this.modelService.deleteModel(modelIds);
		return R.ok();
	}
}
