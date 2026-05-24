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
package com.chestnut.common.utils;

import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 排序工具类
 */
public class SortUtils {

	public enum SortDirection {
		ASC, DESC;
	}

	private static long currentOrder = Instant.now().getEpochSecond();

	public static synchronized long getDefaultSortValue() {
		if (Instant.now().getEpochSecond() <= currentOrder) {
			currentOrder++;
		} else {
			currentOrder = Instant.now().getEpochSecond();
		}
		return currentOrder * 100;
	}

	public static <T> void sort(T source, T target, boolean beforeTarget, SortDirection direction, Function<T, Long> sortValueGetter
			, BiConsumer<T, Long> sortValueSetter, Function<T, Long> lessThanTargetSortValue, Function<T, Long> greaterThanTargetSortValue) {
		long targetSortV = sortValueGetter.apply(target);
		if (direction == SortDirection.DESC) {
			if (beforeTarget) {
				Long greaterThanTargetSortV = greaterThanTargetSortValue.apply(target);
				if (greaterThanTargetSortV < 0) {
					// 倒序移动到最前
					sortValueSetter.accept(source, targetSortV + 10000);
				} else {
					sortValueSetter.accept(source, targetSortV + ((greaterThanTargetSortV - targetSortV) / 2));
				}
			} else {
				Long lessThanTargetSortV = lessThanTargetSortValue.apply(target);
				if (lessThanTargetSortV < 0) {
					// 倒序移动到末尾
					sortValueSetter.accept(source, targetSortV - 10000);
				} else {
					sortValueSetter.accept(source, targetSortV + ((targetSortV - lessThanTargetSortV) / 2));
				}
			}
		} else {
			if (beforeTarget) {
				Long lessThanTargetSortV = lessThanTargetSortValue.apply(target);
				if (lessThanTargetSortV < 0) {
					// 正序移动到最前面
					sortValueSetter.accept(source, targetSortV - 10000);
				} else {
					sortValueSetter.accept(source, targetSortV - ((targetSortV - lessThanTargetSortV) / 2));
				}
			} else {
				Long greaterThanTargetSortV = greaterThanTargetSortValue.apply(target);
				if (greaterThanTargetSortV < 0) {
					// 正序移动到末尾
					sortValueSetter.accept(source, targetSortV + 10000);
				} else {
					sortValueSetter.accept(source, targetSortV + ((greaterThanTargetSortV - targetSortV) / 2));
				}
			}
		}
	}
}
