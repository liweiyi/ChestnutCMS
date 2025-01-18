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

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.ErrorProneWord;

import java.util.Map;

public interface IErrorProneWordService extends IService<ErrorProneWord> {

	/**
	 * 易错词替换
	 * 
	 * @param text 处理文本
	 */
	String replaceErrorProneWords(String text);

	/**
	 * 添加易错词
	 */
	void addErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 修改易错词
	 */
	void updateErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 查找指定文本内容中的易错词
	 */
    Map<String, String> check(String text);

    void sync();
}