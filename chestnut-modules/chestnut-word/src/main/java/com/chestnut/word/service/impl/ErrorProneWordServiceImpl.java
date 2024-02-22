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
package com.chestnut.word.service.impl;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.SensitiveWord;
import com.chestnut.word.sensitive.ErrorProneWordProcessor;
import com.chestnut.word.sensitive.SensitiveWordProcessor;
import com.chestnut.word.sensitive.SensitiveWordType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.word.domain.ErrorProneWord;
import com.chestnut.word.exception.WordErrorCode;
import com.chestnut.word.mapper.ErrorProneWordMapper;
import com.chestnut.word.service.IErrorProneWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorProneWordServiceImpl extends ServiceImpl<ErrorProneWordMapper, ErrorProneWord>
		implements IErrorProneWordService, CommandLineRunner {

	private final ErrorProneWordProcessor processor;

	private static final String CACHE_KEY = "err_prone_word:";

	private final RedisCache redisCache;

	/**
	 * 查找置顶文本中的易错词
	 *
	 * @param text
	 * @return
	 */
	@Override
	public Map<String, String> findErrorProneWords(String text) {
		return this.getErrorProneWords().entrySet().stream().filter(e -> text.indexOf(e.getKey()) > -1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	@Override
	public Map<String, String> check(String text) {
		return this.processor.listWords(text);
	}

	@Override
	public String replaceErrorProneWords(String text) {
		return this.processor.replace(text, ErrorProneWordProcessor.MatchType.MAX);
	}

	@Override
	public Map<String, String> getErrorProneWords() {
		Map<String, String> cacheMap = this.redisCache.getCacheObject(CACHE_KEY, () -> this.list().stream()
				.collect(Collectors.toMap(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord)));
		return cacheMap;
	}

	@Override
	public void addErrorProneWord(ErrorProneWord word) {
		Long count = this.lambdaQuery().eq(ErrorProneWord::getWord, word.getWord()).count();
		Assert.isTrue(count == 0, () -> WordErrorCode.CONFLIECT_ERROR_PRONE_WORD.exception(word.getWord()));

		word.setWordId(IdUtils.getSnowflakeId());
		this.save(word);
		this.processor.addWord(word.getWord(), word.getReplaceWord());
	}

	@Override
	public void updateErrorProneWord(ErrorProneWord word) {
		ErrorProneWord db = this.getById(word.getWordId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", word.getWordId()));

		Long count = this.lambdaQuery().eq(ErrorProneWord::getWord, word.getWord())
				.ne(ErrorProneWord::getWordId, word.getWordId()).count();
		Assert.isTrue(count == 0, () -> WordErrorCode.CONFLIECT_ERROR_PRONE_WORD.exception(word.getWord()));

		String oldWord = db.getWord();

		db.setWord(word.getWord());
		db.setReplaceWord(word.getReplaceWord());
		db.setRemark(word.getRemark());
		db.updateBy(word.getUpdateBy());
		this.updateById(db);
		this.processor.removeWord(oldWord);
		this.processor.addWord(db.getWord(), db.getReplaceWord());
	}

	@Override
	public void run(String... args) {
		List<ErrorProneWord> wordList = this.lambdaQuery()
				.select(List.of(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord)).list();
		this.processor.addWords(wordList.stream().collect(Collectors
				.toMap(ErrorProneWord::getWord, ErrorProneWord::getReplaceWord)));
	}
}
