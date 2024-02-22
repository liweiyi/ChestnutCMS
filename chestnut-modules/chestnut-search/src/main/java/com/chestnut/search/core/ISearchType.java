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
package com.chestnut.search.core;

import com.chestnut.search.domain.dto.SearchModelDTO;

import java.io.IOException;
import java.util.List;

public interface ISearchType {
	
	/**
	 * 检索类型
	 */
	String getType();

	/**
	 * 添加索引
	 *
	 * @param dto
	 */
	boolean addIndex(SearchModelDTO dto) throws IOException;

	/**
	 * 删除索引
	 *
	 * @param indexName
	 */
	boolean deleteIndex(String indexName) throws IOException;
	
	/**
	 * 添加索引文档
	 */
	void addDocument(String indexName, List<BaseDocument> docs) throws  IOException;
	
	/**
	 * 更新索引文档
	 */
	void updateDocument(String indexName, List<BaseDocument> docs) throws IOException;
	
	/**
	 * 删除索引文档
	 */
	void deleteDocument(String indexName, List<String> documentIds) throws IOException;
}
