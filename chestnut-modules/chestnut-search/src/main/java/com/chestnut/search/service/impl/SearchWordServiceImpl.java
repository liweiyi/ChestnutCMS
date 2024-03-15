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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.SearchWordHourStat;
import com.chestnut.search.domain.dto.SearchWordToppingDTO;
import com.chestnut.search.mapper.SearchWordMapper;
import com.chestnut.search.service.ISearchWordHourStatService;
import com.chestnut.search.service.ISearchWordService;
import com.chestnut.system.SysConstants;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class SearchWordServiceImpl extends ServiceImpl<SearchWordMapper, SearchWord>
		implements ISearchWordService {

	private static final String CACHE_KEY = "search:word";

	private final AsyncTaskManager asyncTaskManager;

	private final RedisCache redisCache;

	private final RedissonClient redissonClient;

	private final ISearchWordHourStatService searchWordHourStatService;

	@Override
	public void increaseSearchCount(SearchWord searchWord) {
		if (!redisCache.isValueInZset(CACHE_KEY, searchWord.getWordId().toString())) {
			redisCache.zsetIncr(CACHE_KEY, searchWord.getWordId().toString(),
					searchWord.getSearchTotal() + 1);
		} else {
			redisCache.zsetIncr(CACHE_KEY, searchWord.getWordId().toString(), 1);
		}
	}

	@Override
	public void addWord(SearchWord word) {
		Long count = this.lambdaQuery().eq(SearchWord::getWord, word.getWord()).count();
		Assert.isTrue(count == 0, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		word.setWordId(IdUtils.getSnowflakeId());
		word.setTopFlag(0L);
		this.save(word);
	}

	@Override
	public void editWord(SearchWord word) {
		Long count = this.lambdaQuery().eq(SearchWord::getWord, word.getWord())
				.ne(SearchWord::getWord, word.getWord()).count();
		Assert.isTrue(count == 0, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		SearchWord dbWord = this.getById(word.getWordId());
		dbWord.setWord(word.getWord());
		dbWord.setSearchTotal(word.getSearchTotal());
		dbWord.setTopFlag(word.getTopFlag());
		dbWord.setTopDate(word.getTopDate());
		dbWord.updateBy(word.getUpdateBy());
		this.updateById(dbWord);
	}

	@Override
	public void deleteWords(List<Long> wordIds) {
		// 删除搜索词数据
		this.removeBatchByIds(wordIds);
		// 删除小时统计数据
		asyncTaskManager.execute(() -> {
			searchWordHourStatService.remove(new LambdaQueryWrapper<SearchWordHourStat>()
					.in(SearchWordHourStat::getWordId, wordIds));
		});
	}

	@Override
	public void setTop(SearchWordToppingDTO dto) {
		List<SearchWord> wordStats = this.listByIds(dto.getWordIds());
		for (SearchWord wordStat : wordStats) {
			wordStat.setTopFlag(Instant.now().toEpochMilli());
			wordStat.setTopDate(dto.getTopEndTime());
			wordStat.updateBy(dto.getOperator().getUsername());
		}
		this.updateBatchById(wordStats);
	}

	@Override
	public void cancelTop(List<Long> wordIds, String operator) {
		this.lambdaUpdate().set(SearchWord::getTopFlag, 0L)
				.set(SearchWord::getTopDate, null)
				.set(SearchWord::getUpdateBy, operator)
				.set(SearchWord::getUpdateTime, LocalDateTime.now())
				.in(SearchWord::getWordId, wordIds)
				.update();
	}

	@Override
	public void syncSearchWordStatToDB() {
		RLock lock = redissonClient.getLock("SyncSearchWordStat");
		lock.lock();
		try {
			Set<String> cacheMap = redisCache.getZset(CACHE_KEY, 0, -1);
			cacheMap.forEach(wordId -> {
				SearchWord searchWord = this.getById(wordId);
				if (Objects.nonNull(searchWord)) {
					long searchTotal = redisCache.getZsetScore(CACHE_KEY, wordId).longValue();
					this.lambdaUpdate().set(SearchWord::getSearchTotal, searchTotal)
							.eq(SearchWord::getWordId, wordId).update();
				}
			});
			redisCache.deleteObject(CACHE_KEY);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public SearchWord getSearchWord(String word, String source) {
		RLock lock = redissonClient.getLock("CreateSearchWord");
		lock.lock();
		try {
			Optional<SearchWord> opt = this.lambdaQuery()
					.eq(SearchWord::getWord, word)
					.eq(StringUtils.isNotEmpty(source), SearchWord::getSource, source)
					.isNull(StringUtils.isEmpty(source), SearchWord::getSource)
					.oneOpt();
			if (opt.isPresent()) {
				return opt.get();
			}
			SearchWord searchWord = new SearchWord();
			searchWord.setWordId(IdUtils.getSnowflakeId());
			searchWord.setWord(word);
			searchWord.setSource(source);
			searchWord.setTopFlag(0L);
			searchWord.setSearchTotal(0L);
			searchWord.createBy(SysConstants.SYS_OPERATOR);
			this.save(searchWord);
			return searchWord;
		} finally {
			lock.unlock();
		}
	}
}