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
package com.chestnut.search.job;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.SearchWordHourStat;
import com.chestnut.search.service.ISearchWordHourStatService;
import com.chestnut.search.service.ISearchWordService;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索词统计任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + SearchWordStatJobHandler.JOB_NAME)
public class SearchWordStatJobHandler extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "SearchWordStatJob";

	private final ISearchWordService searchWordService;

	private final ISearchWordHourStatService searchWordHourStatService;

	private final RedisCache redisCache;

	@Override
	public String getId() {
		return JOB_NAME;
	}

	@Override
	public String getName() {
		return "{SCHEDULED_TASK." + JOB_NAME + "}";
	}

	@Override
	public void exec() throws Exception {
		logger.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
		// 更新累计搜索数据
		searchWordService.syncSearchWordStatToDB();
		// 数据更新
		String hour = LocalDateTime.now().format(DateUtils.FORMAT_YYYYMMDDHH);
		this.saveToDb(hour, false);
		// 尝试更新上一个小时数据并删除cache
		String yesterday = LocalDateTime.now().minusHours(1).format(DateUtils.FORMAT_YYYYMMDDHH);
		this.saveToDb(yesterday, true);
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	private void saveToDb(String hour, boolean deleteCache) {
		String cacheKey = ISearchWordHourStatService.CACHE_PREFIX + hour;

		Map<Long, SearchWordHourStat> stats = this.searchWordHourStatService.lambdaQuery()
				.eq(SearchWordHourStat::getHour, hour)
				.list()
				.stream()
				.collect(Collectors.toMap(SearchWordHourStat::getWordId, stat -> stat));
		// TODO 大量数据需要优化
		Map<Long, String> searchWords = this.searchWordService.lambdaQuery()
				.select(List.of(SearchWord::getWordId, SearchWord::getWord))
				.list()
				.stream()
				.collect(Collectors.toMap(SearchWord::getWordId, SearchWord::getWord));
		Set<Long> insertIds = new HashSet<>();
		Map<String, Integer> cacheMap = this.redisCache.getCacheMap(cacheKey);
		cacheMap.forEach((wordId, count) -> {
			Long wordIdLongV = Long.valueOf(wordId);
			if (searchWords.containsKey(wordIdLongV)) {
				SearchWordHourStat stat = stats.get(wordIdLongV);
				if (Objects.isNull(stat)) {
					stat = new SearchWordHourStat();
					stat.setHour(hour);
					stat.setWordId(wordIdLongV);
					stat.setWord(searchWords.get(wordIdLongV));

					stats.put(wordIdLongV, stat);
					insertIds.add(wordIdLongV);
				}
				stat.setSearchCount(Math.max(count, 0));
			}
		});
		// 更新数据库
		List<SearchWordHourStat> inserts = stats.values().stream()
				.filter(stat -> insertIds.contains(stat.getWordId())).toList();
		this.searchWordHourStatService.saveBatch(inserts);
		List<SearchWordHourStat> updates = stats.values().stream()
				.filter(stat -> !insertIds.contains(stat.getWordId())).toList();
		this.searchWordHourStatService.updateBatchById(updates);
		// 清理过期缓存
		if (deleteCache) {
			this.redisCache.deleteObject(cacheKey);
		}
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
