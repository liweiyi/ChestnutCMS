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
package com.chestnut.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.SensitiveWord;
import com.chestnut.word.domain.dto.CreateSensitiveWordRequest;

import java.util.List;
import java.util.Set;

public interface ISensitiveWordService extends IService<SensitiveWord> {
	
	/**
	 * 替换敏感词
	 * 
	 * @param text
	 * @param replacement
	 * @return
	 */
	String replaceSensitiveWords(String text, String replacement);

	/**
	 * 添加敏感词
	 * 
	 * @param req
	 * @return
	 */
	void addWord(CreateSensitiveWordRequest req);

	/**
	 * 删除敏感词
	 * 
	 * @param wordIds
	 * @return
	 */
	void deleteWord(List<Long> wordIds);

	/**
	 * 查找指定内容中的敏感词
	 *
	 * @param text
	 * @return
	 */
	Set<String> check(String text);

    void sync();
}
