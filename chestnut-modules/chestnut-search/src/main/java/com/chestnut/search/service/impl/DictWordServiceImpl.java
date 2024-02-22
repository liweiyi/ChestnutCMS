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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.domain.DictWord;
import com.chestnut.search.domain.dto.DictWordDTO;
import com.chestnut.search.exception.SearchErrorCode;
import com.chestnut.search.mapper.DictWordMapper;
import com.chestnut.search.service.IDictWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DictWordServiceImpl extends ServiceImpl<DictWordMapper, DictWord> implements IDictWordService {

	private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private static final String MODIFY_CACHE_KEY = "search:dict:modify:";

	private final RedisCache redisCache;

	@Override
	public void batchAddDictWords(DictWordDTO dto) {
		List<DictWord> dictWords = new ArrayList<>();
		for (String word : dto.getWords()) {
			if (StringUtils.isBlank(word)) {
				continue;
			}
			Long count = this.lambdaQuery().eq(DictWord::getWord, word.trim()).count();
			Assert.isTrue(count == 0, () -> SearchErrorCode.DICT_WORD_EXISTS.exception(word));
			
			DictWord dictWord = new DictWord();
			dictWord.setWordId(IdUtils.getSnowflakeId());
			dictWord.setWordType(dto.getWordType());
			dictWord.setWord(word);
			dictWord.createBy(dto.getOperator().getUsername());
			dictWords.add(dictWord);
		}
		this.saveBatch(dictWords);
		this.redisCache.setCacheObject(MODIFY_CACHE_KEY + dto.getWordType(), LocalDateTime.now().format(FORMATTER));
	}

	@Override
	public void deleteDictWord(List<Long> dictWordIds) {
		List<DictWord> list = this.listByIds(dictWordIds);
		Set<String> wordTypes = new HashSet<>();
		for (DictWord dictWord : list) {
			wordTypes.add(dictWord.getWordType());
		}
		this.removeByIds(dictWordIds);
		wordTypes.forEach(wordType -> this.redisCache.setCacheObject(MODIFY_CACHE_KEY + wordType,
				LocalDateTime.now().format(FORMATTER)));
	}

	@Override
	public String getLastModified(String wordType) {
		return this.redisCache.getCacheObject(MODIFY_CACHE_KEY + wordType);
	}
}