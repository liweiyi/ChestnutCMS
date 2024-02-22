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
package com.chestnut.word.service;

import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.ErrorProneWord;

public interface IErrorProneWordService extends IService<ErrorProneWord> {

	/**
	 * 查找置顶文本中的易错词
	 * 
	 * @param text
	 * @return
	 */
	Map<String, String> findErrorProneWords(String text);

	/**
	 * 易错词替换
	 * 
	 * @param str
	 * @return
	 */
	String replaceErrorProneWords(String text);

	/**
	 * 易错词集合
	 * 
	 * @return
	 */
	Map<String, String> getErrorProneWords();

	/**
	 * 添加易错词
	 * 
	 * @param errorProneWord
	 */
	void addErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 修改易错词
	 * 
	 * @param errorProneWord
	 */
	void updateErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 查找指定文本内容中的易错词
	 *
	 * @param text
	 * @return
	 */
    Map<String, String> check(String text);
}