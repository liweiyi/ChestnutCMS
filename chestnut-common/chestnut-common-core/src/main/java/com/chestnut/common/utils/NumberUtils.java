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
