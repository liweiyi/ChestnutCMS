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
package com.chestnut.word;

public interface WordConstants {

	/**
	 * 敏感词默认替换字符串
	 */
	public String SENSITIVE_WORD_REPLACEMENT = "*";
	
	/**
	 * 热词链接替换模板字符串
	 */
	public String HOT_WORD_REPLACEMENT = "<a class=\"hot-word\" href=\"{0}\" target=\"{2}\">{1}</a>";
}
