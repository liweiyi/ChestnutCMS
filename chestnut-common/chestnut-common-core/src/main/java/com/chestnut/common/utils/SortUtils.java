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
