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
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.XModelFieldDTO;
import com.chestnut.xmodel.service.IModelFieldService;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 元数据模型字段前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/xmodel/field")
public class XModelFieldController extends BaseRestController {

	private final IModelService modelService;

	private final IModelFieldService modelFieldService;

	@GetMapping("/list")
	public R<?> getModelFieldList(@RequestParam @LongId Long modelId,
								  @RequestParam(value = "query", required = false) String query) {
		PageRequest pr = getPageRequest();
		Page<XModelField> page = modelFieldService.lambdaQuery()
				.eq(XModelField::getModelId, modelId)
				.like(StringUtils.isNotEmpty(query), XModelField::getName, query)
				.orderByAsc(XModelField::getSortFlag).orderByAsc(XModelField::getFieldId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/all")
	public R<?> listAllModelFields(@RequestParam @LongId Long modelId,
								   @RequestParam(required = false, defaultValue = "true") Boolean ignoreFixedField) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		List<MetaModelField> fields = new ArrayList<>(model.getFields());
		if (!ignoreFixedField) {
			fields.addAll(XModelUtils.getMetaModelType(model.getModel().getOwnerType()).getFixedFields());
		}
		return this.bindDataTable(fields);
	}

	@Log(title = "新增元数据字段", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@RequestBody @Validated XModelFieldDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelFieldService.addModelField(dto);
		return R.ok();
	}

	@Log(title = "编辑元数据字段", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@RequestBody @Validated XModelFieldDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelFieldService.editModelField(dto);
		return R.ok();
	}

	@Log(title = "删除元数据字段", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> fieldIds) {
		this.modelFieldService.deleteModelField(fieldIds);
		return R.ok();
	}
}
