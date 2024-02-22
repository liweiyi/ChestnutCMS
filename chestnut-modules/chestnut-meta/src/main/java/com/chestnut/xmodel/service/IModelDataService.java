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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chestnut.common.db.util.SqlBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface IModelDataService {

	/**
	 * 根据主键获取模型数据
	 *
	 * @param modelId
	 * @param pkValues
	 * @return
	 */
	Map<String, Object> getModelDataByPkValue(Long modelId, Map<String, Object> pkValues);

	/**
	 * 保存模型数据
	 * 
	 * @param modelId
	 * @param params
	 * @return
	 */
	void saveModelData(Long modelId, Map<String, Object> params);

	/**
	 * 添加元数据模型数据
	 *
	 * @param modelId
	 * @param data
	 */
    void addModelData(Long modelId, Map<String, Object> data);

	/**
	 * 更新元数据模型数据
	 *
	 * @param modelId
	 * @param data
	 */
	void updateModelData(Long modelId, Map<String, Object> data);

	/**
	 * 删除元数据模型数据
	 *
	 * @param modeId
	 * @param pkValues
	 */
	void deleteModelDataByPkValue(Long modeId, List<Map<String, String>> pkValues);

    List<Map<String, Object>> selectModelDataList(Long modelId, Consumer<SqlBuilder> consumer);

	IPage<Map<String, Object>> selectModelDataPage(Long modelId, IPage<Map<String, Object>> page,
												   Consumer<SqlBuilder> consumer);
}
