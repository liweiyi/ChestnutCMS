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
package com.chestnut.xmodel.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.dto.XModelDTO;

public interface IModelService extends IService<XModel> {

	/**
	 * 添加扩展模型
	 * 
	 * @param dto
	 * @return
	 */
	void addModel(XModelDTO dto);
	
	/**
	 * 编辑扩展模型
	 * 
	 * @param dto
	 * @return
	 */
	void editModel(XModelDTO dto);
	
	/**
	 * 删除扩展模型
	 * 
	 * @param modelIds
	 * @return
	 */
	void deleteModel(List<Long> modelIds);

	/**
	 * 模型数据自定义表
	 */
	List<String> listModelDataTables(String type);

	/**
	 * 模型数据表字段
	 */
	List<String> listModelTableFields(XModel model);

	/**
	 * 元数据模型定义缓存
	 */
	MetaModel getMetaModel(Long modelId);

	/**
	 * 清理元数据模型定义缓存
	 */
	void clearMetaModelCache(Long modelId);
}
