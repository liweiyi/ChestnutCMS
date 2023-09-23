package com.chestnut.common.utils;

import java.util.ArrayList;

public class NumberUtils extends org.apache.commons.lang3.math.NumberUtils {

	/**
	 * 数字转二进制高位数组
	 * 
	 * @param num
	 * @return
	 */
	public static ArrayList<Integer> getBinaryList(int num) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		String binStr = Integer.toBinaryString(num);
		for (int i = 0; i < binStr.length(); i++) {
			if (binStr.charAt(i) == '1') {
				int intpow = (int) Math.pow(2, binStr.length() - i - 1);
				list.add(intpow);
			}
		}
		return list;
	}
}
