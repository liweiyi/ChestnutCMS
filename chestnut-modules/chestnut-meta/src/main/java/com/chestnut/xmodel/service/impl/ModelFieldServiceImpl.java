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
package com.chestnut.xmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.db.DBService;
import com.chestnut.common.db.domain.DBTable;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.ArrayUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.CreateXModelFieldRequest;
import com.chestnut.xmodel.dto.UpdateXModelFieldRequest;
import com.chestnut.xmodel.exception.MetaErrorCode;
import com.chestnut.xmodel.fixed.dict.MetaFieldType;
import com.chestnut.xmodel.mapper.XModelFieldMapper;
import com.chestnut.xmodel.service.IModelFieldService;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelFieldServiceImpl extends ServiceImpl<XModelFieldMapper, XModelField> implements IModelFieldService {

	private final IModelService modelService;

	private final DBService dbService;

	@Override
	public void addModelField(CreateXModelFieldRequest req) {
		XModel model = this.modelService.getById(req.getModelId());
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", req.getModelId()));
		this.checkFieldCodeUnique(req.getModelId(), req.getCode(), null);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		boolean isDefaultTable = mmt.getDefaultTable().equals(model.getTableName());
		String[] usedFields = this.getUsedFields(model.getModelId(), req.getFieldType(), isDefaultTable);
		if (isDefaultTable) {
			int fieldTypeLimit = MetaFieldType.getFieldTypeLimit(req.getFieldType());
			Assert.isTrue(fieldTypeLimit > usedFields.length,
					() -> MetaErrorCode.FIELD_LIMIT.exception(req.getFieldType()));

			for (int i = 1; i <= fieldTypeLimit; i++) {
				if (!ArrayUtils.contains(req.getFieldType() + i, usedFields)) {
					req.setFieldName(req.getFieldType() + i);
					break;
				}
			}
		} else {
			List<String> fixedFields = mmt.getFixedFields().stream().map(MetaModelField::getFieldName).toList();
			if (fixedFields.contains(req.getFieldName())) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(req.getFieldName());
			}
			if (ArrayUtils.contains(req.getFieldName(), usedFields)) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(req.getFieldName());
			}
			if (!isTableContainsColumn(model.getTableName(), req.getFieldName())) {
				throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(req.getFieldName());
			}
		}
		XModelField xModelField = new XModelField();
		BeanUtils.copyProperties(req, xModelField);
		xModelField.setFieldId(IdUtils.getSnowflakeId());
		xModelField.createBy(req.getOperator().getUsername());
		this.save(xModelField);

		this.modelService.clearMetaModelCache(model.getModelId());
	}

	@Override
	public void editModelField(UpdateXModelFieldRequest req) {
		XModelField modelField = this.getById(req.getFieldId());
		Assert.notNull(modelField, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("fieldId", req.getFieldId()));
		this.checkFieldCodeUnique(req.getModelId(), req.getCode(), req.getFieldId());

		String oldFieldName = modelField.getFieldName();
		String oldFieldType = modelField.getFieldType();

		XModel model = this.modelService.getById(modelField.getModelId());
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		boolean isDefaultTable = mmt.getDefaultTable().equals(model.getTableName());
		if (isDefaultTable && !req.getFieldType().equals(oldFieldType)) {
			// 字段种类变更，重新计算是否有可用字段
			String[] usedFields = this.getUsedFields(model.getModelId(), req.getFieldType(), true);
			int fieldTypeLimit = MetaFieldType.getFieldTypeLimit(req.getFieldType());
			Assert.isTrue(fieldTypeLimit > usedFields.length,
					() -> MetaErrorCode.FIELD_LIMIT.exception(req.getFieldType()));

			for (int i = 1; i <= fieldTypeLimit; i++) {
				if (!ArrayUtils.contains(req.getFieldType() + i, usedFields)) {
					req.setFieldName(req.getFieldType() + i);
					break;
				}
			}
		} else if (!isDefaultTable && !req.getFieldName().equals(oldFieldName)) {
			List<String> fixedFields = mmt.getFixedFields().stream().map(MetaModelField::getFieldName).toList();
			if (fixedFields.contains(req.getFieldName())) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(req.getFieldName());
			}
			String[] usedFields = this.getUsedFields(model.getModelId(), req.getFieldType(), false);
			if (ArrayUtils.contains(req.getFieldName(), usedFields)) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(req.getFieldName());
			}
			if (!isTableContainsColumn(model.getTableName(), req.getFieldName())) {
				throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(req.getFieldName());
			}
		}
		BeanUtils.copyProperties(req, modelField, "modelId");
		modelField.updateBy(req.getOperator().getUsername());
		this.updateById(modelField);

		this.modelService.clearMetaModelCache(model.getModelId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteModelField(List<Long> fieldIds) {
		List<XModelField> fields = this.listByIds(fieldIds);
		this.removeByIds(fieldIds);

		fields.stream().map(XModelField::getModelId).collect(Collectors.toSet())
				.forEach(this.modelService::clearMetaModelCache);
	}

	/**
	 * 判断指定表`tableName`是否含有指定字段`columnName`
	 *
	 * @param tableName 表名
	 * @param columnName 字段名
	 */
	private boolean isTableContainsColumn(String tableName, String columnName) {
		List<DBTable> dbTables = this.dbService.listTables(tableName);
		if (dbTables.isEmpty()) {
			return false;
		}
		return dbTables.get(0).getColumns().stream()
				.anyMatch(tc -> tc.getName().equals(columnName));
	}

	/**
	 * 获取指定模型已配置过的数据库字段名
	 *
	 * @param modelId 元数据模型ID
	 * @param fieldType 字段类型
	 * @param isDefaultTable 是否默认数据表
	 */
	private String[] getUsedFields(Long modelId, String fieldType, boolean isDefaultTable) {
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>()
				.eq(XModelField::getModelId, modelId)
				.eq(isDefaultTable, XModelField::getFieldType, fieldType);
		List<XModelField> list = this.list(q);
		return list.stream().map(XModelField::getFieldName).toArray(String[]::new);
	}

	/**
	 * 校验字段编码是否已存在
	 */
	private void checkFieldCodeUnique(Long modelId, String code, Long fieldId) {
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>()
				.eq(XModelField::getModelId, modelId)
				.eq(XModelField::getCode, code)
				.ne(IdUtils.validate(fieldId), XModelField::getFieldId, fieldId);
		Assert.isTrue(this.count(q) == 0, () -> CommonErrorCode.DATA_CONFLICT.exception("code"));
	}
}
