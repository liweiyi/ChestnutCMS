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
}
