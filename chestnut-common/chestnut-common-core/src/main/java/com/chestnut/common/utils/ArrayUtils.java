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

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public class ArrayUtils {

	/**
	 * 在数组arr中查找与searchStr值相等的第一个元素，返回元素所在位置索引
	 * 
	 * @param searchStr
	 * @param arr
	 * @return
	 */
	public static int indexOf(String searchStr, String... arr) {
		if (Objects.isNull(arr) || arr.length == 0) {
			return -1;
		}
		for (int i = 0; i < arr.length; i++) {
			if (searchStr == null) {
				if (arr[i] == null) {
					return i;
				}
			} else {
				if (searchStr.equals(arr[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean contains(String searchStr, String... arr) {
		return indexOf(searchStr, arr) > -1;
	}

	/**
	 * 查找指定列表中符合条件的第一个元素并返回，如果没有符合条件的元素直接抛出异常
	 */
	public static <T> T first(Collection<T> list, Predicate<T> predicate) {
		if (Objects.nonNull(list)) {
			for (T item : list) {
				if (predicate.test(item)) {
					return item;
				}
			}
		}
		throw new NullPointerException("No matched item in list.");
	}

	/**
	 * 查找指定列表中符合条件的第一个元素并返回，如果没有符合条件的元素返回NULL
	 */
	public static <T> T firstOrNull(Collection<T> list, Predicate<T> predicate) {
		if (Objects.nonNull(list)) {
			for (T item : list) {
				if (predicate.test(item)) {
					return item;
				}
			}
		}
		return null;
	}
}
