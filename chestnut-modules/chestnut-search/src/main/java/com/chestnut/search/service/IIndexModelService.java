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
package com.chestnut.search.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.search.domain.IndexModel;
import com.chestnut.search.domain.dto.SearchModelDTO;

public interface IIndexModelService extends IService<IndexModel> {
	
	/**
	 * 根据模型编码获取包含模型字段的模型实例缓存
	 * 
	 * @param modelCode
	 * @return
	 */
	SearchModelDTO getIndexModel(String modelCode);

	/**
	 * 添加索引模型
	 * 
	 * @param model
	 */
	void addIndexModel(SearchModelDTO dto);
	
	/**
	 * 删除索引模型
	 * 
	 * @param modelIds
	 */
	void deleteIndexModel(List<Long> modelIds);
}