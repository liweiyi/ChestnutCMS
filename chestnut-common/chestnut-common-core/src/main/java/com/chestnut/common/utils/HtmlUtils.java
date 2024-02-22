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

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 转义和反转义工具类
 */
public class HtmlUtils {
	
	/**
	 * 转义文本中的HTML字符为安全的字符
	 * 
	 * @param text 被转义的文本
	 * @return 转义后的文本
	 */
	public static String escape(String text) {
		return StringEscapeUtils.escapeHtml4(text);
	}
	
	public static String[] escapeArr(String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = escape(arr[i]);
		}
		return arr;
	}

	/**
	 * 还原被转义的HTML特殊字符
	 * 
	 * @param text 包含转义符的HTML内容
	 * @return 转换后的字符串
	 */
	public static String unescape(String text) {
		return StringEscapeUtils.unescapeHtml4(text);
	}

	/**
	 * 清除所有HTML标签，但是不删除标签内的内容
	 * 
	 * @param content 文本
	 * @return 清除标签后的文本
	 */
	public static String clean(String content) {
		return Jsoup.clean(content, Safelist.simpleText());
	}
}