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
package com.chestnut.search.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.SearchWordHourStat;
import com.chestnut.search.mapper.SearchWordHourStatMapper;
import com.chestnut.search.service.ISearchWordHourStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SearchWordHourStatServiceImpl extends ServiceImpl<SearchWordHourStatMapper, SearchWordHourStat>
		implements ISearchWordHourStatService {

	private final RedisCache redisCache;

	private final AsyncTaskManager asyncTaskManager;

	public void handleSearchLog(SearchWord wordStat, LocalDateTime evtTime) {
		this.asyncTaskManager.execute(() -> {
			// redis 小时检索数+1
			String cacheKey = CACHE_PREFIX + evtTime.format(DateUtils.FORMAT_YYYYMMDDHH);
			redisCache.incrMapValue(cacheKey, wordStat.getWordId().toString(), 1);
		});
	}
}