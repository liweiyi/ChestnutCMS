package com.chestnut.xmodel.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chestnut.common.db.util.SqlBuilder;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.domain.XModel;
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

	private final IModelService modelService;

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
			if (f.isMandatory() && Objects.isNull(value)) {
				throw new RuntimeException("Validate field: " + f.getCode() + " cannot be empty.");
			}
			fieldValues.put(f.getFieldName(), value.toString());
		});
		// 自定义字段
		model.getFields().forEach(field -> {
			Object fieldValue = data.get(field.getCode());
			if (fieldValue == null) {
				fieldValue = field.getDefaultValue();
			} else if (fieldValue.getClass().isArray()) {
				Object[] arr = (Object[]) fieldValue;
				fieldValue = StringUtils.join(arr, StringUtils.COMMA);
			} else if (fieldValue instanceof List<?> list) {
				fieldValue = StringUtils.join(list, StringUtils.COMMA);
			}
			// 必填校验
			if (field.isMandatory() && (Objects.isNull(fieldValue) || StringUtils.isEmpty(fieldValue.toString()))) {
				throw new RuntimeException("Validate field: " + field.getCode() + " cannot be empty.");
			}
			// TODO 其它自定义规则校验
			fieldValues.put(field.getFieldName(), Objects.isNull(fieldValue) ? "" : fieldValue.toString());
		});
		return fieldValues;
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
			dataMap.put(f.getCode(), MapUtils.getString(map, f.getFieldName(), StringUtils.EMPTY));
		});
		// 自定义字段
		model.getFields().forEach(f -> {
			dataMap.put(f.getCode(), MapUtils.getString(map, f.getFieldName(), StringUtils.EMPTY));
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
