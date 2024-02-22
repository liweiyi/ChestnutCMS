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

import java.time.Instant;

/**
 * 排序工具类
 */
public class SortUtils {

	private static long currentOrder = Instant.now().getEpochSecond();

	public static synchronized long getDefaultSortValue() {
		if (Instant.now().getEpochSecond() <= currentOrder) {
			currentOrder++;
		} else {
			currentOrder = Instant.now().getEpochSecond();
		}
		return currentOrder * 100;
	}
}
