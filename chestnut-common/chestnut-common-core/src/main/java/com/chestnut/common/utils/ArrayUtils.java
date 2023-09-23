package com.chestnut.common.utils;

import java.util.Objects;

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
}
