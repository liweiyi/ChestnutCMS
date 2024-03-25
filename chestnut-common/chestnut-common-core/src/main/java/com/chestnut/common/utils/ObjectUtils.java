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

import java.util.Objects;

public class ObjectUtils {

	/**
	 * 数组中是否存在任意对象为空
	 * 
	 * @param objects
	 * @return
	 */
	public static boolean isAnyNull(Object... objects) {
		if (Objects.isNull(objects)) {
			return true;
		}
		for (Object object : objects) {
			if (Objects.isNull(object)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 数组中指定位置元素是否不为空
	 * 
	 * @param objects
	 * @param index
	 * @return
	 */
	public static boolean nonNull(Object[] objects, int index) {
		if (Objects.isNull(objects) || objects.length <= index) {
			return false;
		}
		return Objects.nonNull(objects[index]);
	}

	/**
	 * 指定对象是否为null或者转成string后为空字符串
	 *
	 * @param obj
	 * @return
	 */
    public static boolean isNullOrEmptyStr(Object obj) {
		return Objects.isNull(obj) || StringUtils.isEmpty(obj.toString());
    }
}
