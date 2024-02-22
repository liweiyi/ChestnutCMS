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
package com.chestnut.xmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.db.DBService;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.db.domain.DBTable;
import com.chestnut.common.utils.ArrayUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.XModelFieldDTO;
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
	public void addModelField(XModelFieldDTO dto) {
		XModel model = this.modelService.getById(dto.getModelId());
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", dto.getModelId()));

		dto.setFieldId(null);
		if (!this.checkFieldCodeUnique(dto)) {
			throw CommonErrorCode.DATA_CONFLICT.exception("code");
		}
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		boolean isDefaultTable = mmt.getDefaultTable().equals(model.getTableName());
		String[] usedFields = this.getUsedFields(model.getModelId(), dto.getFieldType(), isDefaultTable);
		if (isDefaultTable) {
			int fieldTypeLimit = MetaFieldType.getFieldTypeLimit(dto.getFieldType());
			Assert.isTrue(fieldTypeLimit > usedFields.length,
					() -> MetaErrorCode.FIELD_LIMIT.exception(dto.getFieldType()));

			for (int i = 1; i <= fieldTypeLimit; i++) {
				if (!ArrayUtils.contains(dto.getFieldType() + i, usedFields)) {
					dto.setFieldName(dto.getFieldType() + i);
					break;
				}
			}
		} else {
			List<String> fixedFields = mmt.getFixedFields().stream().map(MetaModelField::getFieldName).toList();
			if (fixedFields.contains(dto.getFieldName())) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(dto.getFieldName());
			}
			if (ArrayUtils.contains(dto.getFieldName(), usedFields)) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(dto.getFieldName());
			}
			if (!isTableContainsColumn(model.getTableName(), dto.getFieldName())) {
				throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(dto.getFieldName());
			}
		}
		XModelField xModelField = new XModelField();
		BeanUtils.copyProperties(dto, xModelField, "fieldId");
		xModelField.setFieldId(IdUtils.getSnowflakeId());
		xModelField.createBy(dto.getOperator().getUsername());
		this.save(xModelField);

		this.modelService.clearMetaModelCache(model.getModelId());
	}

	@Override
	public void editModelField(XModelFieldDTO dto) {
		XModelField modelField = this.getById(dto.getFieldId());
		Assert.notNull(modelField, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("fieldId", dto.getFieldId()));

		if (!this.checkFieldCodeUnique(dto)) {
			throw CommonErrorCode.DATA_CONFLICT.exception("code");
		}
		String oldFieldName = modelField.getFieldName();
		String oldFieldType = modelField.getFieldType();

		XModel model = this.modelService.getById(dto.getModelId());
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		boolean isDefaultTable = mmt.getDefaultTable().equals(model.getTableName());
		if (isDefaultTable && !dto.getFieldType().equals(oldFieldType)) {
			// 字段种类变更，重新计算是否有可用字段
			String[] usedFields = this.getUsedFields(model.getModelId(), dto.getFieldType(), true);
			int fieldTypeLimit = MetaFieldType.getFieldTypeLimit(dto.getFieldType());
			Assert.isTrue(fieldTypeLimit > usedFields.length,
					() -> MetaErrorCode.FIELD_LIMIT.exception(dto.getFieldType()));

			for (int i = 1; i <= fieldTypeLimit; i++) {
				if (!ArrayUtils.contains(dto.getFieldType() + i, usedFields)) {
					dto.setFieldName(dto.getFieldType() + i);
					break;
				}
			}
		} else if (!isDefaultTable && !dto.getFieldName().equals(oldFieldName)) {
			List<String> fixedFields = mmt.getFixedFields().stream().map(MetaModelField::getFieldName).toList();
			if (fixedFields.contains(dto.getFieldName())) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(dto.getFieldName());
			}
			String[] usedFields = this.getUsedFields(model.getModelId(), dto.getFieldType(), false);
			if (ArrayUtils.contains(dto.getFieldName(), usedFields)) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(dto.getFieldName());
			}
			if (!isTableContainsColumn(model.getTableName(), dto.getFieldName())) {
				throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(dto.getFieldName());
			}
		}
		BeanUtils.copyProperties(dto, modelField, "fieldId", "modelId");
		modelField.updateBy(dto.getOperator().getUsername());
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
	private boolean checkFieldCodeUnique(XModelFieldDTO dto) {
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>()
				.eq(XModelField::getModelId, dto.getModelId())
				.eq(XModelField::getCode, dto.getCode())
				.ne(dto.getFieldId() != null && dto.getFieldId() > 0, XModelField::getFieldId, dto.getFieldId());
		return this.count(q) == 0;
	}
}
