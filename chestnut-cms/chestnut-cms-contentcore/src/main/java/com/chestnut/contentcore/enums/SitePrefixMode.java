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
package com.chestnut.contentcore.enums;

/**
 * 站点路径模式，决定模板上下文${Prefix}取值
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class SitePrefixMode {

	/**
	 * 相对路径，Prefix = "/"
	 */
	public static final String Relative = "relative";

	/**
	 * 绝对路径， Prefix = "SiteUrl"
	 */
	public static final String Absolute = "absolute";

	public static boolean isRelative(String v) {
		return Relative.equals(v);
	}
	
	public static boolean isAbsolute(String v) {
		return Absolute.equals(v);
	}
}
