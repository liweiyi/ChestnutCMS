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
package com.chestnut.xmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.*;
import com.chestnut.xmodel.exception.MetaErrorCode;
import com.chestnut.xmodel.exception.MetaXValidationException;
import com.chestnut.xmodel.fixed.dict.MetaFieldType;
import com.chestnut.xmodel.mapper.MetaModelDataMapper;
import com.chestnut.xmodel.mapper.support.MetaModelDataSqlCommand;
import com.chestnut.xmodel.mapper.support.MetaModelDataSqlCommandFactory;
import com.chestnut.xmodel.service.IModelDataService;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ModelDataServiceImpl implements IModelDataService {

	private final Map<String, IMetaControlType> controlTypeMap;

	private final Map<String, IMetaFieldValidation> fieldValidationMap;

	private final IModelService modelService;

	private final MetaModelDataMapper modelDataMapper;

	private final MetaModelDataSqlCommandFactory sqlCommandFactory;

	private IMetaControlType getControlType(String type) {
		return controlTypeMap.get(IMetaControlType.BEAN_PREFIX + type);
	}

	private IMetaFieldValidation getFieldValidation(String type) {
		return fieldValidationMap.get(IMetaFieldValidation.BEAN_PREFIX + type);
	}

	@Override
	public void saveModelData(Long modelId, Map<String, Object> params) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		List<MetaModelField> primaryKeys = this.getPrimaryKeys(mmt);

		long count;
		if (this.isDefaultDataTable(model, mmt)) {
			QueryWrapper<BaseModelData> wrapper = this.newDefaultQueryWrapper(mmt);
			this.addPrimaryKeyConditions(wrapper, primaryKeys, params);
			count = Db.count(wrapper);
		} else {
			count = this.modelDataMapper.selectCount(this.newCustomCommand(model, Map.of(),
					this.getPrimaryKeyConditions(primaryKeys, params)));
		}
		if (count > 0) {
			this.updateModelData(modelId, params);
		} else {
			this.addModelData(modelId, params);
		}
	}

	@Override
	public void addModelData(Long modelId, Map<String, Object> data) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		final Map<String, Object> fieldValues = this.parseFieldValues(model, data);
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		if (this.isDefaultDataTable(model, mmt)) {
			BaseModelData modelData = this.newDefaultData(mmt);
			fieldValues.forEach(modelData::setFieldValue);
			Db.save(modelData);
		} else {
			this.modelDataMapper.insert(this.newCustomCommand(model, fieldValues, Map.of()));
		}
	}

	@Override
	public void updateModelData(Long modelId, Map<String, Object> data) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		final Map<String, Object> fieldValues = this.parseFieldValues(model, data);

		List<MetaModelField> primaryKeys = this.getPrimaryKeys(mmt);
		// 移除可能存在的主键字段值
		primaryKeys.forEach(f -> fieldValues.remove(f.getFieldName()));
		if (this.isDefaultDataTable(model, mmt)) {
			UpdateWrapper<BaseModelData> wrapper = Wrappers.update(this.newDefaultData(mmt)).checkSqlInjection();
			fieldValues.forEach(wrapper::set);
			this.addPrimaryKeyConditions(wrapper, primaryKeys, data);
			Db.update(wrapper);
		} else {
			this.modelDataMapper.update(this.newCustomCommand(model, fieldValues,
					this.getPrimaryKeyConditions(primaryKeys, data)));
		}
	}

	private Map<String, Object> parseFieldValues(MetaModel model, Map<String, Object> data) {
		final Map<String, Object> fieldValues = new HashMap<>();
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		// 固定字段
		mmt.getFixedFields().forEach(f -> {
			Object value = data.get(f.getCode());
			this.validateFieldValue(f.getName(), value, f.getValidations());
			value = MetaFieldType.parse(f.getFieldType(), value);
			fieldValues.put(f.getFieldName(), value);
		});
		// 自定义字段
		model.getFields().forEach(field -> {
			Object fieldValue = data.get(field.getCode());
			// Long, Double, Date, String, String[], Object[]
			IMetaControlType controlType = getControlType(field.getControlType());
			if (Objects.nonNull(fieldValue)) {
				fieldValue = controlType.valueAsString(fieldValue);
			}
			if (Objects.isNull(fieldValue) || fieldValue.toString().isBlank()) {
				fieldValue = StringUtils.isBlank(field.getDefaultValue()) ? null : field.getDefaultValue();
			}
			// 校验
			this.validateFieldValue(field.getName(), fieldValue, field.getValidations());
			fieldValue = MetaFieldType.parse(field.getFieldType(), fieldValue);
			fieldValues.put(field.getFieldName(), fieldValue);
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
	public void deleteModelDataByPkValue(Long modelId, List<Map<String, Object>> pkValues) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());

		List<MetaModelField> primaryKeys = this.getPrimaryKeys(mmt);

		pkValues.forEach(pkValue -> {
			if (this.isDefaultDataTable(model, mmt)) {
				QueryWrapper<BaseModelData> wrapper = this.newDefaultQueryWrapper(mmt);
				this.addPrimaryKeyConditions(wrapper, primaryKeys, pkValue);
				Db.remove(wrapper);
			} else {
				this.modelDataMapper.delete(this.newCustomCommand(model, Map.of(),
						this.getPrimaryKeyConditions(primaryKeys, pkValue)));
			}
		});
	}

	@Override
	public Map<String, Object> getModelDataByPkValue(Long modelId, Map<String, Object> pkValues) {
		MetaModel model = this.modelService.getMetaModel(modelId);

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());

		List<MetaModelField> primaryKeys = this.getPrimaryKeys(mmt);
		Object[] args = primaryKeys.stream().map(f -> pkValues.get(f.getCode())).toArray(Object[]::new);
		if (ObjectUtils.isAnyNull(args)) {
			return Map.of();
		}
		Map<String, Object> map;
		if (this.isDefaultDataTable(model, mmt)) {
			QueryWrapper<BaseModelData> wrapper = this.newDefaultQueryWrapper(mmt);
			this.addPrimaryKeyConditions(wrapper, primaryKeys, pkValues);
			map = Db.getMap(wrapper);
		} else {
			map = this.modelDataMapper.selectOne(this.newCustomCommand(model, Map.of(),
					this.getPrimaryKeyConditions(primaryKeys, pkValues)));
		}
		Map<String, Object> dataMap = new HashMap<>();
		if (map == null) {
			model.getFields().forEach(f -> {
				dataMap.put(f.getCode(), getControlType(f.getControlType()).stringAsValue(StringUtils.EMPTY));
			});
			return dataMap;
		}
		Map<String, Object> row = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		row.putAll(map);
		// 固定字段
		mmt.getFixedFields().forEach(f -> {
			Object v = row.get(f.getFieldName());
			dataMap.put(f.getCode(), v);
		});
		// 自定义字段
		model.getFields().forEach(f -> {
			Object v = row.get(f.getFieldName());
			IMetaControlType controlType = getControlType(f.getControlType());
            if (Objects.isNull(v)) {
				v = StringUtils.EMPTY;
			}
            Object objectV = v;
            if (v instanceof String) {
			    objectV = controlType.stringAsValue(v.toString());
            }
			dataMap.put(f.getCode(), objectV);
		});
		return dataMap;
	}

	@Override
	public List<Map<String, Object>> selectModelDataList(Long modelId, Consumer<MetaModelDataQuery> consumer) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		MetaModelDataQuery query = this.createQuery(consumer);

		List<Map<String, Object>> rows;
		if (this.isDefaultDataTable(model, mmt)) {
			QueryWrapper<BaseModelData> wrapper = this.newDefaultQueryWrapper(mmt)
					.eq(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId());
			this.addQueryConditions(wrapper, model, mmt, query);
			rows = Db.listMaps(wrapper);
		} else {
			rows = this.modelDataMapper.selectList(this.newCustomCommand(model, Map.of(),
					this.getQueryConditions(model, mmt, query)));
		}
		return this.mapFieldNamesToCodes(rows, model, mmt);
	}

	@Override
	public IPage<Map<String, Object>> selectModelDataPage(Long modelId, IPage<Map<String, Object>> page,
														  Consumer<MetaModelDataQuery> consumer) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
		MetaModelDataQuery query = this.createQuery(consumer);

		if (this.isDefaultDataTable(model, mmt)) {
			QueryWrapper<BaseModelData> wrapper = this.newDefaultQueryWrapper(mmt)
					.eq(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId());
			this.addQueryConditions(wrapper, model, mmt, query);
			Db.pageMaps(page, wrapper);
		} else {
			this.modelDataMapper.selectPage(page, this.newCustomCommand(model, Map.of(),
					this.getQueryConditions(model, mmt, query)));
		}
		page.setRecords(this.mapFieldNamesToCodes(page.getRecords(), model, mmt));
		return page;
	}

	private List<MetaModelField> getPrimaryKeys(IMetaModelType mmt) {
		List<MetaModelField> primaryKeys = mmt.getFixedFields().stream()
				.filter(MetaModelField::isPrimaryKey).toList();
		if (primaryKeys.isEmpty()) {
			throw new RuntimeException("Meta model primary key not defined.");
		}
		return primaryKeys;
	}

	private Object getPrimaryKeyValue(Map<String, Object> values, MetaModelField primaryKey) {
		Object value = values.get(primaryKey.getCode());
		if (Objects.isNull(value)) {
			throw new RuntimeException("Meta model primary key `" + primaryKey.getCode() + "` value cannot be null.");
		}
		return value;
	}

	private boolean isDefaultDataTable(MetaModel model, IMetaModelType mmt) {
		return mmt.getDefaultTable().equalsIgnoreCase(model.getModel().getTableName());
	}

	@SuppressWarnings("unchecked")
	private Class<BaseModelData> getDefaultDataClass(IMetaModelType mmt) {
		return (Class<BaseModelData>) mmt.getDefaultDataClass();
	}

	private BaseModelData newDefaultData(IMetaModelType mmt) {
		return BeanUtils.instantiateClass(this.getDefaultDataClass(mmt));
	}

	private QueryWrapper<BaseModelData> newDefaultQueryWrapper(IMetaModelType mmt) {
		return Wrappers.query(this.getDefaultDataClass(mmt)).checkSqlInjection();
	}

	private void addPrimaryKeyConditions(QueryWrapper<BaseModelData> wrapper,
										 List<MetaModelField> primaryKeys, Map<String, Object> values) {
		primaryKeys.forEach(primaryKey -> wrapper.eq(primaryKey.getFieldName(),
				this.getPrimaryKeyValue(values, primaryKey)));
	}

	private void addPrimaryKeyConditions(UpdateWrapper<BaseModelData> wrapper,
										 List<MetaModelField> primaryKeys, Map<String, Object> values) {
		primaryKeys.forEach(primaryKey -> wrapper.eq(primaryKey.getFieldName(),
				this.getPrimaryKeyValue(values, primaryKey)));
	}

	private Map<String, Object> getPrimaryKeyConditions(List<MetaModelField> primaryKeys,
													 Map<String, Object> values) {
		Map<String, Object> conditions = new LinkedHashMap<>();
		primaryKeys.forEach(primaryKey -> conditions.put(primaryKey.getFieldName(),
				this.getPrimaryKeyValue(values, primaryKey)));
		return conditions;
	}

	private MetaModelDataQuery createQuery(Consumer<MetaModelDataQuery> consumer) {
		MetaModelDataQuery query = new MetaModelDataQuery();
		if (consumer != null) {
			consumer.accept(query);
		}
		return query;
	}

	private String resolveFieldName(MetaModel model, IMetaModelType mmt, String fieldCode) {
		return Stream.concat(mmt.getFixedFields().stream(), model.getFields().stream())
				.filter(field -> field.getCode().equals(fieldCode))
				.map(MetaModelField::getFieldName)
				.findFirst()
				.orElseThrow(() -> MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(fieldCode));
	}

	private void addQueryConditions(QueryWrapper<BaseModelData> wrapper, MetaModel model,
									IMetaModelType mmt, MetaModelDataQuery query) {
		query.getConditions().forEach(condition -> {
			String fieldName = this.resolveFieldName(model, mmt, condition.fieldCode());
			if (condition.value() == null) {
				wrapper.isNull(fieldName);
			} else {
				wrapper.eq(fieldName, condition.value());
			}
		});
	}

	private Map<String, Object> getQueryConditions(MetaModel model, IMetaModelType mmt,
													 MetaModelDataQuery query) {
		Map<String, Object> conditions = new LinkedHashMap<>();
		conditions.put(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId());
		query.getConditions().forEach(condition -> {
			String fieldName = this.resolveFieldName(model, mmt, condition.fieldCode());
			conditions.put(fieldName, condition.value());
		});
		return conditions;
	}

	private MetaModelDataSqlCommand newCustomCommand(MetaModel model, Map<String, Object> values,
													  Map<String, Object> conditions) {
		return this.sqlCommandFactory.create(model.getModel().getTableName(), values, conditions);
	}

	private List<Map<String, Object>> mapFieldNamesToCodes(List<Map<String, Object>> rows,
														 MetaModel model, IMetaModelType mmt) {
		Map<String, String> fieldNameToCode = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		fieldNameToCode.putAll(model.getFields().stream()
				.collect(Collectors.toMap(MetaModelField::getFieldName, MetaModelField::getCode)));
		mmt.getFixedFields().forEach(f -> fieldNameToCode.put(f.getFieldName(), f.getCode()));

		return rows.stream().map(data -> {
			Map<String, Object> result = new HashMap<>();
			data.forEach((fieldName, value) -> {
				String fieldCode = fieldNameToCode.get(fieldName);
				if (fieldCode != null) {
					result.put(fieldCode, value);
				}
			});
			return result;
		}).toList();
	}
}
