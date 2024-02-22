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
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.db.DBService;
import com.chestnut.common.db.domain.DBTable;
import com.chestnut.common.db.domain.DBTableColumn;
import com.chestnut.common.db.util.SqlBuilder;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.core.impl.MetaControlType_Input;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.XModelDTO;
import com.chestnut.xmodel.exception.MetaErrorCode;
import com.chestnut.xmodel.mapper.XModelFieldMapper;
import com.chestnut.xmodel.mapper.XModelMapper;
import com.chestnut.xmodel.service.IModelService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl extends ServiceImpl<XModelMapper, XModel>
		implements IModelService, CommandLineRunner {

	private static final String CACHE_PREFIX = "xmodel:";

	private final XModelFieldMapper modelFieldMapper;

	private final DBService dbService;

	private final RedisCache redisCache;

	@Override
	public MetaModel getMetaModel(Long modelId) {
		MetaModel model = this.redisCache.getCacheObject(CACHE_PREFIX + modelId, () -> {
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
		this.redisCache.deleteObject(CACHE_PREFIX + modelId);
	}

	@Override
	public List<String> listModelDataTables(String type) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(type);
		List<String> list = new ArrayList<>();
		// 数据表
		this.dbService.listTables(null).forEach(t -> {
			if (t.getName().startsWith(mmt.getTableNamePrefix())) {
				list.add(t.getName());
			}
		});
		return list;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addModel(XModelDTO dto) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(dto.getOwnerType());
		if (StringUtils.isEmpty(dto.getTableName())) {
			dto.setTableName(mmt.getDefaultTable());
		}
		if (!mmt.getDefaultTable().equalsIgnoreCase(dto.getTableName())) {
			List<DBTable> dbTables = this.dbService.listTables(dto.getTableName());
			Assert.isFalse(dbTables.isEmpty(), () -> MetaErrorCode.META_TABLE_NOT_EXISTS.exception(dto.getTableName()));
		}
		XModel model = new XModel();
		BeanUtils.copyProperties(dto, model, "modelId");
		model.setModelId(IdUtils.getSnowflakeId());
		model.createBy(dto.getOperator().getUsername());
		this.save(model);

		List<String> fixedFields = mmt.getFixedFields().stream().map(MetaModelField::getFieldName).toList();
		// 自定义表直接初始化非固定字段
		if (!mmt.getDefaultTable().equalsIgnoreCase(dto.getTableName())) {
			List<DBTable> tables = this.dbService.listTables(dto.getTableName());
			if (!tables.isEmpty()) {
				List<DBTableColumn> listTableColumn = tables.get(0).getColumns();
				for (DBTableColumn column : listTableColumn) {
					if (!fixedFields.contains(column.getName())) {
						XModelField field = new XModelField();
						field.setFieldId(IdUtils.getSnowflakeId());
						field.setModelId(model.getModelId());
						field.setName(StringUtils.firstNotBlankStr(column.getLabel(), column.getComment(), column.getName()));
						field.setCode(column.getName());
						field.setFieldName(column.getName());
						field.setControlType(MetaControlType_Input.TYPE);
						field.setDefaultValue(column.getDefaultValue());
						field.createBy(dto.getOperator().getUsername());
						this.modelFieldMapper.insert(field);
					}
				}
			}
		}
	}

	@Override
	public void editModel(XModelDTO dto) {
		XModel model = this.getById(dto.getModelId());
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", dto.getModelId()));

		BeanUtils.copyProperties(dto, model, "modelId", "ownerType", "ownerId", "tableName");
		model.updateBy(dto.getOperator().getUsername());
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
				new SqlBuilder().delete().from(model.getModel().getTableName()).where()
						.eq(IMetaModelType.MODEL_ID_FIELD_NAME, model.getModel().getModelId())
						.executeDelete();
				// 清理缓存
				this.clearMetaModelCache(model.getModel().getModelId());
			}
		}
	}

	@Override
	public List<String> listModelTableFields(XModel model) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		if (model.getTableName().equals(mmt.getDefaultTable())) {
			return new ArrayList<>();
		}
		List<DBTable> dbTables = this.dbService.listTables(model.getTableName());
		if (dbTables.isEmpty()) {
			return List.of();
		}
		return dbTables.get(0).getColumns().stream().map(DBTableColumn::getName).toList();
	}

	@Override
	public void run(String... args) {
		XModelUtils.validateMetaModelTypes();
	}
}
