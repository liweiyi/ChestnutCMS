/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.search.domain.SearchWordHourStat;

import java.time.LocalDateTime;

public interface ISearchWordHourStatService extends IService<SearchWordHourStat> {

	String CACHE_PREFIX = "search:word:hour:";

	/**
	 * 处理搜索日志
	 */
	void handleSearchLog(SearchWord wordStat, LocalDateTime evtTime);
}