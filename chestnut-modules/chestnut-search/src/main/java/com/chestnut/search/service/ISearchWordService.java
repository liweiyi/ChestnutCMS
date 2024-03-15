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

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.dto.SearchWordToppingDTO;

import java.util.List;

public interface ISearchWordService extends IService<SearchWord> {

	/**
	 * 搜索词计数
	 */
	void increaseSearchCount(SearchWord searchWord);

	/**
	 * 创建搜索词数据
	 */
	void addWord(SearchWord word);

	/**
	 * 编辑搜索词数据
	 */
	void editWord(SearchWord word);

	/**
	 * 删除搜索词数据
	 */
	void deleteWords(List<Long> wordIds);

	/**
	 * 搜索词置顶
	 */
	void setTop(SearchWordToppingDTO dto);

	/**
	 * 搜索词取消置顶
	 */
	void cancelTop(List<Long> wordIds, String operator);

	/**
	 * 同步缓存搜索词计数数据到DB
	 */
    void syncSearchWordStatToDB();

	SearchWord getSearchWord(String word, String source);
}