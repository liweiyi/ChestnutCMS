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
package com.chestnut.common.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 转义和反转义工具类
 */
public class HtmlUtils extends org.springframework.web.util.HtmlUtils {

	/**
	 * 清除所有HTML标签，但是不删除标签内的内容
	 * 
	 * @param content 文本
	 * @return 清除标签后的文本
	 */
	public static String clean(String content) {
		return Jsoup.clean(content, Safelist.none());
	}

	public static String clean(String content, Safelist safelist) {
		return Jsoup.clean(content, safelist);
	}
}