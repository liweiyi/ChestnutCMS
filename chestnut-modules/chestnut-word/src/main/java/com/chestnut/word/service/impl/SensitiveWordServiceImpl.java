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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.WordConstants;
import com.chestnut.word.domain.SensitiveWord;
import com.chestnut.word.mapper.SensitiveWordMapper;
import com.chestnut.word.sensitive.SensitiveWordProcessor;
import com.chestnut.word.sensitive.SensitiveWordProcessor.MatchType;
import com.chestnut.word.sensitive.SensitiveWordProcessor.ReplaceType;
import com.chestnut.word.sensitive.SensitiveWordType;
import com.chestnut.word.service.ISensitiveWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
		implements ISensitiveWordService, CommandLineRunner {

	private final SensitiveWordProcessor processor;

	@Override
	public Set<String> check(String text) {
		return this.processor.listWords(text);
	}

	@Override
	public String replaceSensitiveWords(String text, String replacement) {
		if (StringUtils.isBlank(text)) {
			return text;
		}
		if (StringUtils.isBlank(replacement)) {
			replacement = WordConstants.SENSITIVE_WORD_REPLACEMENT;
		}
		return this.processor.replace(text, MatchType.MAX, ReplaceType.WORD, replacement);
	}

	@Override
	public void addWord(SensitiveWord word) {
		word.setWordId(IdUtils.getSnowflakeId());
		word.setCreateTime(LocalDateTime.now());
		this.save(word);

		this.processor.addWord(word.getWord(), SensitiveWordType.valueOf(word.getType()));
	}

	@Override
	public void editWord(SensitiveWord word) {
		SensitiveWord dbSensitiveWord = this.getById(word.getWordId());
		Assert.notNull(dbSensitiveWord,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", word.getWordId()));

		String oldWord = dbSensitiveWord.getWord();
		String oldType = dbSensitiveWord.getType();

		dbSensitiveWord.setWord(word.getWord());
		dbSensitiveWord.setReplaceWord(word.getReplaceWord());
		dbSensitiveWord.setType(word.getType());
		dbSensitiveWord.setRemark(word.getRemark());
		dbSensitiveWord.updateBy(word.getUpdateBy());
		this.updateById(word);

		if (!dbSensitiveWord.getWord().equals(oldWord) || !dbSensitiveWord.getType().equals(oldType)) {
			this.processor.removeWord(oldWord);
			this.processor.addWord(dbSensitiveWord.getWord(), SensitiveWordType.valueOf(dbSensitiveWord.getType()));
		}
	}

	@Override
	public void deleteWord(List<Long> wordIds) {
		List<SensitiveWord> list = this.listByIds(wordIds);
		this.removeByIds(wordIds);

		Set<String> words = list.stream().map(SensitiveWord::getWord).collect(Collectors.toSet());
		this.processor.removeWords(words);
	}

	@Override
	public void run(String... args) throws Exception {
		// 敏感词黑名单
		List<SensitiveWord> blackList = this.lambdaQuery()
				.eq(SensitiveWord::getType, SensitiveWordType.BLACK.name()).list();
		// 敏感词白名单
		List<SensitiveWord> whiteList = this.lambdaQuery()
				.eq(SensitiveWord::getType, SensitiveWordType.WHITE.name()).list();
		this.processor.init(blackList.stream().map(SensitiveWord::getWord).collect(Collectors.toSet()),
				whiteList.stream().map(SensitiveWord::getWord).collect(Collectors.toSet()));
	}
}
