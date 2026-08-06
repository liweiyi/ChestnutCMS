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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.db.DBService;
import com.chestnut.common.db.domain.DBTable;
import com.chestnut.common.db.domain.DBTableColumn;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.cache.XModelMonitoredCache;
import com.chestnut.xmodel.core.BaseModelData;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.core.impl.MetaControlType_Input;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.CreateXModelRequest;
import com.chestnut.xmodel.dto.UpdateXModelRequest;
import com.chestnut.xmodel.exception.MetaErrorCode;
import com.chestnut.xmodel.mapper.MetaModelDataMapper;
import com.chestnut.xmodel.mapper.XModelFieldMapper;
import com.chestnut.xmodel.mapper.XModelMapper;
import com.chestnut.xmodel.mapper.support.MetaModelDataSqlCommandFactory;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl extends ServiceImpl<XModelMapper, XModel>
		implements IModelService, CommandLineRunner {

	private final XModelFieldMapper modelFieldMapper;

	private final DBService dbService;

	private final XModelMonitoredCache modelCache;

	private final MetaModelDataMapper modelDataMapper;

	private final MetaModelDataSqlCommandFactory sqlCommandFactory;

	@Override
	public MetaModel getMetaModel(Long modelId) {
		MetaModel model = modelCache.get(modelId, () -> {
					XModel xmodel = getById(modelId);
					if (xmodel == null) {
						return null;
					}

					List<MetaModelField> fields = new LambdaQueryChainWrapper<>(modelFieldMapper)
							.eq(XModelField::getModelId, modelId)
							.orderByAsc(XModelField::getSortFlag).orderByAsc(XModelField::getFieldId)
							.list()
							.stream().map(MetaModelField::new)
							.toList();
					MetaModel metaModel = new MetaModel();
					metaModel.setModel(xmodel);
					metaModel.setFields(fields);
					return metaModel;
				});
		Assert.notNull(model, () -> MetaErrorCode.META_MODEL_NOT_FOUND.exception(modelId));
		return model;
	}

	@Override
	public void clearMetaModelCache(Long modelId) {
		this.modelCache.clear(modelId);
	}

	@Override
	public List<String> listModelDataTables(String type) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(type);
		List<String> list = new ArrayList<>();
		// 数据表
		this.dbService.listTables(null).forEach(t -> {
			if (t.getName().regionMatches(true, 0, mmt.getTableNamePrefix(), 0,
					mmt.getTableNamePrefix().length())) {
				list.add(t.getName());
			}
		});
		return list;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addModel(CreateXModelRequest req) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(req.getOwnerType());
		List<String> fixedFields = mmt.getFixedFields().stream().map(MetaModelField::getFieldName).toList();
		if (StringUtils.isEmpty(req.getTableName())) {
			req.setTableName(mmt.getDefaultTable());
		}
		DBTable customTable = null;
		if (!mmt.getDefaultTable().equalsIgnoreCase(req.getTableName())) {
			customTable = this.dbService.findTable(req.getTableName()).orElse(null);
			Assert.notNull(customTable, () -> MetaErrorCode.META_TABLE_NOT_EXISTS.exception(req.getTableName()));
			DBTable finalCustomTable = customTable;
			Assert.isTrue(customTable.getName().regionMatches(true, 0, mmt.getTableNamePrefix(), 0,
					mmt.getTableNamePrefix().length()),
					() -> MetaErrorCode.META_TABLE_NOT_ALLOWED.exception(finalCustomTable.getName()));
			for (String fixedField : fixedFields) {
				Assert.isTrue(customTable.getColumns().stream()
						.anyMatch(column -> column.getName().equalsIgnoreCase(fixedField)),
						() -> MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(fixedField));
			}
			req.setTableName(customTable.getName());
		}
		XModel model = new XModel();
		BeanUtils.copyProperties(req, model, "modelId");
		model.setModelId(IdUtils.getSnowflakeId());
		model.createBy(req.getOperator().getUsername());
		this.save(model);

		// 自定义表直接初始化非固定字段
		if (customTable != null) {
			for (DBTableColumn column : customTable.getColumns()) {
				if (fixedFields.stream().noneMatch(field -> field.equalsIgnoreCase(column.getName()))) {
					XModelField field = new XModelField();
					field.setFieldId(IdUtils.getSnowflakeId());
					field.setModelId(model.getModelId());
					field.setName(StringUtils.firstNotBlankStr(column.getLabel(), column.getComment(), column.getName()));
					field.setCode(column.getName());
					field.setFieldName(column.getName());
					field.setControlType(MetaControlType_Input.TYPE);
					field.setDefaultValue(column.getDefaultValue());
					field.createBy(req.getOperator().getUsername());
					this.modelFieldMapper.insert(field);
				}
			}
		}
	}

	@Override
	public void editModel(UpdateXModelRequest req) {
		XModel model = this.getById(req.getModelId());
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", req.getModelId()));

		BeanUtils.copyProperties(req, model, "modelId", "ownerType", "ownerId", "tableName");
		model.updateBy(req.getOperator().getUsername());
		this.updateById(model);
		this.clearMetaModelCache(model.getModelId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteModel(List<Long> modelIds) {
		for (Long modelId : modelIds) {
			MetaModel model = this.getMetaModel(modelId);
			if (model != null) {
				// 移除模型字段数据
				this.modelFieldMapper.delete(new LambdaQueryWrapper<XModelField>().eq(XModelField::getModelId, modelId));
				// 移除模型数据
				this.removeById(model.getModel().getModelId());
				// 移除模型数据表数据
				IMetaModelType mmt = XModelUtils.getMetaModelType(model.getModel().getOwnerType());
				if (mmt.getDefaultTable().equalsIgnoreCase(model.getModel().getTableName())) {
					QueryWrapper<BaseModelData> wrapper = Wrappers.query(this.getDefaultDataClass(mmt))
							.checkSqlInjection()
							.eq(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId());
					Db.remove(wrapper);
				} else {
					this.modelDataMapper.delete(this.sqlCommandFactory.create(model.getModel().getTableName(),
							Map.of(), Map.of(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId())));
				}
				// 清理缓存
				this.clearMetaModelCache(model.getModel().getModelId());
			}
		}
	}

	@Override
	public List<String> listModelTableFields(XModel model) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		if (model.getTableName().equalsIgnoreCase(mmt.getDefaultTable())) {
			return new ArrayList<>();
		}
		DBTable dbTable = this.dbService.findTable(model.getTableName()).orElse(null);
		if (dbTable == null) {
			return List.of();
		}
		return dbTable.getColumns().stream().map(DBTableColumn::getName).toList();
	}

	@Override
	public void run(String @NonNull ... args) {
		XModelUtils.validateMetaModelTypes();
	}

	@SuppressWarnings("unchecked")
	private Class<BaseModelData> getDefaultDataClass(IMetaModelType mmt) {
		return (Class<BaseModelData>) mmt.getDefaultDataClass();
	}
}
