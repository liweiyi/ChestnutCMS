/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.WordConstants;
import com.chestnut.word.cache.SensitiveWordMonitoredCache;
import com.chestnut.word.domain.SensitiveWord;
import com.chestnut.word.domain.dto.CreateSensitiveWordRequest;
import com.chestnut.word.mapper.SensitiveWordMapper;
import com.chestnut.word.sensitive.SensitiveWordProcessor;
import com.chestnut.word.sensitive.SensitiveWordProcessor.MatchType;
import com.chestnut.word.sensitive.SensitiveWordProcessor.ReplaceType;
import com.chestnut.word.sensitive.SensitiveWordType;
import com.chestnut.word.service.ISensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
		implements ISensitiveWordService, CommandLineRunner {

	private final SensitiveWordProcessor processor;

	private final SensitiveWordMonitoredCache wordCache;

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
	public void addWord(CreateSensitiveWordRequest req) {
		SensitiveWord word = new SensitiveWord();
		word.setWordId(IdUtils.getSnowflakeId());
		word.setType(req.getType());
		word.setWord(req.getWord());
		word.setReplaceWord(req.getReplaceWord());
		word.setRemark(req.getRemark());
		word.createBy(req.getOperator().getUsername());
		this.save(word);

		this.processor.addWord(word.getWord(), SensitiveWordType.valueOf(word.getType()));
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
		Set<String> blackList = getSensitiveWords(SensitiveWordType.BLACK);
		// 敏感词白名单
		Set<String> whiteList = getSensitiveWords(SensitiveWordType.WHITE);
		this.processor.init(blackList, whiteList);
	}

	private Set<String> getSensitiveWords(SensitiveWordType type) {
		return wordCache.get(type, () -> this.lambdaQuery()
				.eq(SensitiveWord::getType, type.name())
				.list()
				.stream().map(SensitiveWord::getWord).collect(Collectors.toSet()));
	}

	@Override
	public void sync() {
		Set<String> blackWords = getSensitiveWords(SensitiveWordType.BLACK);
		Set<String> whiteWords = getSensitiveWords(SensitiveWordType.WHITE);
		this.processor.reset(blackWords, whiteWords);
	}
}
