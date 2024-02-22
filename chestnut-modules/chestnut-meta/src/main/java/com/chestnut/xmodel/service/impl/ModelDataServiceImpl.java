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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chestnut.common.db.util.SqlBuilder;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaFieldValidation;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.exception.MetaXValidationException;
import com.chestnut.xmodel.service.IModelDataService;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelDataServiceImpl implements IModelDataService {

	private final Map<String, IMetaFieldValidation> fieldValidationMap;

	private final IModelService modelService;

	private IMetaFieldValidation getFieldValidation(String type) {
		return fieldValidationMap.get(IMetaFieldValidation.BEAN_PREFIX + type);
	}

	@Override
	public void saveModelData(Long modelId, Map<String, Object> params) {
		XModel model = this.modelService.getMetaModel(modelId).getModel();

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		List<MetaModelField> primaryKeys = mmt.getFixedFields().stream()
				.filter(MetaModelField::isPrimaryKey).toList();
		if (primaryKeys.isEmpty()) {
			throw new RuntimeException("Meta model primary key not defined.");
		}
		SqlBuilder sqlBuilder = new SqlBuilder().selectAll().from(model.getTableName()).where();
		for (int i = 0; i < primaryKeys.size(); i++) {
			if (i > 0) {
				sqlBuilder.and();
			}
			MetaModelField pkField = primaryKeys.get(i);
			Object fieldValue = params.get(pkField.getCode());
			if (Objects.isNull(fieldValue)) {
				throw new RuntimeException("Meta model primary key `" + pkField.getCode() + "` value cannot be null.");
			}
			sqlBuilder.eq(pkField.getFieldName(), fieldValue);
		}
		long count = sqlBuilder.selectCount();
		if (count > 0) {
			this.updateModelData(modelId, params);
		} else {
			this.addModelData(modelId, params);
		}
	}

	@Override
	public void addModelData(Long modelId, Map<String, Object> data) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		final Map<String, String> fieldValues = this.parseFieldValues(model, data);
		// 构建插入sql添加数据
		SqlBuilder sqlBuilder = new SqlBuilder().insertInto(model.getModel().getTableName(),
				fieldValues.keySet(), fieldValues.values());
		sqlBuilder.execInsert();
	}

	@Override
	public void updateModelData(Long modelId, Map<String, Object> data) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		final Map<String, String> fieldValues = this.parseFieldValues(model, data);

		List<MetaModelField> primaryKeys = mmt.getFixedFields().stream()
				.filter(MetaModelField::isPrimaryKey).toList();
		// 移除可能存在的主键字段值
		primaryKeys.forEach(f -> fieldValues.remove(f.getFieldName()));
		// 更新数据
		SqlBuilder sqlBuilder = new SqlBuilder().update(model.getModel().getTableName());
		fieldValues.forEach(sqlBuilder::set);
		sqlBuilder.where();
		for (int i = 0; i < primaryKeys.size(); i++) {
			if (i > 0) {
				sqlBuilder.and();
			}
			MetaModelField pkField = primaryKeys.get(i);
			Object fieldValue = data.get(pkField.getCode());
			if (Objects.isNull(fieldValue)) {
				throw new RuntimeException("Meta model primary key `"+pkField.getCode()+"` value cannot be null.");
			}
			sqlBuilder.eq(pkField.getFieldName(), fieldValue);
		}
		sqlBuilder.executeUpdate();
	}

	private Map<String, String> parseFieldValues(MetaModel model, Map<String, Object> data) {
		final Map<String, String> fieldValues = new HashMap<>();
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		// 固定字段
		mmt.getFixedFields().forEach(f -> {
			Object value = data.get(f.getCode());
			this.validateFieldValue(f.getName(), value, f.getValidations());
			fieldValues.put(f.getFieldName(), value.toString());
		});
		// 自定义字段
		model.getFields().forEach(field -> {
			Object fieldValue = data.get(field.getCode());
			if (Objects.nonNull(fieldValue)) {
				if (fieldValue.getClass().isArray()) {
					Object[] arr = (Object[]) fieldValue;
					fieldValue = StringUtils.join(arr, StringUtils.COMMA);
				} else if (fieldValue instanceof List<?> list) {
					fieldValue = StringUtils.join(list, StringUtils.COMMA);
				}
			}
			if (Objects.isNull(fieldValue) || fieldValue.toString().isBlank()) {
				fieldValue = StringUtils.isBlank(field.getDefaultValue()) ? null : field.getDefaultValue();
			}
			// 校验
			this.validateFieldValue(field.getName(), fieldValue, field.getValidations());
			fieldValues.put(field.getFieldName(), Objects.isNull(fieldValue) ? null : fieldValue.toString());
		});
		return fieldValues;
	}

	private void validateFieldValue(String fieldName, Object fieldValue, List<Map<String, Object>> validations) {
		if (StringUtils.isNotEmpty(validations)) {
			for (Map<String, Object> validation : validations) {
				IMetaFieldValidation fieldValidation = this.getFieldValidation(MapUtils.getString(validation, "type"));
				boolean validate = fieldValidation.validate(fieldValue, validation);
				Assert.isTrue(validate, () -> new MetaXValidationException(fieldValidation.getErrorMessage(fieldName)));
			}
		}
	}

	@Override
	public void deleteModelDataByPkValue(Long modelId, List<Map<String, String>> pkValues) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());

		List<MetaModelField> primaryKeys = mmt.getFixedFields().stream()
				.filter(MetaModelField::isPrimaryKey).toList();

		pkValues.forEach(pkValue -> {
			SqlBuilder sqlBuilder = new SqlBuilder().delete().from(model.getModel().getTableName()).where();
			for (int i = 0; i < primaryKeys.size(); i++) {
				if (i > 0) {
					sqlBuilder.and();
				}
				MetaModelField pkField = primaryKeys.get(i);
				String fieldValue = pkValue.get(pkField.getCode());
				if (Objects.isNull(fieldValue)) {
					throw new RuntimeException("Primary key `"+pkField.getCode()+"` cannot be null!");
				}
				sqlBuilder.eq(pkField.getFieldName(), fieldValue);
			}
			sqlBuilder.executeDelete();
		});
	}

	@Override
	public Map<String, Object> getModelDataByPkValue(Long modelId, Map<String, Object> pkValues) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());

		List<MetaModelField> primaryKeys = mmt.getFixedFields().stream()
				.filter(MetaModelField::isPrimaryKey).toList();
		Object[] args = primaryKeys.stream().map(f -> pkValues.get(f.getCode())).toArray(Object[]::new);
		if (ObjectUtils.isAnyNull(args)) {
			return Map.of();
		}
		SqlBuilder sqlBuilder = new SqlBuilder().selectAll().from(model.getModel().getTableName()).where();
		for (int i = 0; i < primaryKeys.size(); i++) {
			if (i > 0) {
				sqlBuilder.and();
			}
			MetaModelField pkField = primaryKeys.get(i);
			sqlBuilder.eq(pkField.getFieldName(), pkValues.get(pkField.getCode()));
		}
		Map<String, Object> map = sqlBuilder.selectOne();
		if (map == null) {
			return Map.of();
		}
		Map<String, Object> dataMap = new HashMap<>();
		// 固定字段
		mmt.getFixedFields().forEach(f -> {
			dataMap.put(f.getCode(), map.get(f.getFieldName()));
		});
		// 自定义字段
		model.getFields().forEach(f -> {
			dataMap.put(f.getCode(), map.get(f.getFieldName()));
		});
		return dataMap;
	}

	@Override
	public List<Map<String, Object>> selectModelDataList(Long modelId, Consumer<SqlBuilder> consumer) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		Map<String, String> fieldNameToCode = model.getFields().stream()
				.collect(Collectors.toMap(MetaModelField::getFieldName, MetaModelField::getCode));
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		mmt.getFixedFields().forEach(f -> fieldNameToCode.put(f.getFieldName(), f.getCode()));

		SqlBuilder sqlBuilder = new SqlBuilder().selectAll().from(model.getModel().getTableName())
				.where().eq(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId());
		consumer.accept(sqlBuilder);

		return sqlBuilder.selectList().stream().map(data -> {
			Map<String, Object> map = new HashMap<>();
			data.forEach((key, value) -> map.put(fieldNameToCode.get(key), value));
			return map;
		}).toList();
	}

	@Override
	public IPage<Map<String, Object>> selectModelDataPage(Long modelId, IPage<Map<String, Object>> page,
														  Consumer<SqlBuilder> consumer) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		Map<String, String> fieldNameToCode = model.getFields().stream()
				.collect(Collectors.toMap(MetaModelField::getFieldName, MetaModelField::getCode));
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		mmt.getFixedFields().forEach(f -> fieldNameToCode.put(f.getFieldName(), f.getCode()));

		SqlBuilder sqlBuilder = new SqlBuilder().selectAll().from(model.getModel().getTableName())
				.where().eq(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId());
		consumer.accept(sqlBuilder);

		IPage<Map<String, Object>> pageData = sqlBuilder.selectPage(page);
		List<Map<String, Object>> list = pageData.getRecords().stream().map(data -> {
			Map<String, Object> map = new HashMap<>();
			data.forEach((key, value) -> map.put(fieldNameToCode.get(key), value));
			return map;
		}).toList();
		pageData.setRecords(list);
		return pageData;
	}
}
