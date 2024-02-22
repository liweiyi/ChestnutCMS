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

import java.util.Arrays;

import org.apache.commons.lang3.RandomUtils;

/**
 * 可复原随机码工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CDKeyUtil {

	/**
	 * 32位随机字符串
	 */
	private static final char[] CHARS_32 = new char[] { '8', 'L', 'A', 'W', 'B', 'X', 'C', '4', '9', 'Z', 'M', 'T', '7',
			'Y', 'R', '6', '2', 'H', 'S', 'F', 'D', 'V', 'E', 'J', '3', 'K', 'Q', 'P', 'U', 'G', 'N', '5' };

	/**
	 * 56位随机字符串，大小写字母+数字，排除易混淆字母和数字
	 */
	private static final char[] CHARS_56 = new char[] { 'n', '8', 'x', 'L', 'f', 'A', 'z', 'W', 'u', 'B', 'e', 'X', 'C',
			'4', '9', 'Z', 'j', 'M', 'q', 'T', '7', 'i', 'y', 'Y', 'p', 'R', '6', 'h', 'c', '2', 'v', 'H', 'm', 'S',
			'b', 'F', 't', 'D', 'V', 'g', 'E', 'a', 'J', '3', 'K', 'w', 'Q', 'P', 'd', 'k', 'U', 'r', 'G', 'N', '5',
			's' };

	/**
	 * 纯数字
	 */
	private static final char[] CHARS_10 = new char[] { '8', '4', '9', '7', '6', '2', '3', '0', '1', '5' };

	/**
	 * 随机盐
	 */
	private final static long SLAT = 151476L;
//	private final static long SLAT = 1234L;

	/**
	 * PRIME1 与 CHARS 的长度 L互质，可保证 ( id * PRIME1) % L 在 [0,L)上均匀分布
	 */
	private final static int PRIME1 = 3;

	/**
	 * PRIME2 与 CODE_LENGTH 互质，可保证 ( index * PRIME2) % CODE_LENGTH 在
	 * [0，CODE_LENGTH）上均匀分布
	 */
	private final static int PRIME2 = 11;

	/**
	 * 生成随机码，包含数字和大写字母，长度限制：[6, 12]
	 * 
	 * @param seq
	 * @param codeLength
	 * @return
	 */
	public static String genChar32(long seq, int codeLength) {
		if (codeLength < 6 || codeLength > 12) {
			throw new RuntimeException("Invalid code length, recommend: 6, 8, 10, 12.");
		}
		long maxSeq = (long) ((Math.pow(32, codeLength - 1) - SLAT) / PRIME1);
		if (seq <= 0 || seq > maxSeq) {
			throw new RuntimeException("Maximum seq value exceeded, max = " + maxSeq + ", seq = " + seq);
		}
		return encode(seq, codeLength, CHARS_32);
	}

	/**
	 * 生成随机码，包含大小写字母和数字，长度限制：[6, 10]
	 * 
	 * @param seq
	 * @param codeLength
	 * @return
	 */
	public static String genChar56(long seq, int codeLength) {
		if (codeLength < 6 || codeLength > 10) {
			throw new RuntimeException("Invalid code length, recommend: 6, 8, 10.");
		}
		long maxSeq = (long) ((Math.pow(56, codeLength - 1) - SLAT) / PRIME1);
		if (seq <= 0 || seq > maxSeq) {
			throw new RuntimeException("Maximum seq value exceeded, max = " + maxSeq + ", seq = " + seq);
		}
		return encode(seq, codeLength, CHARS_56);
	}

	/**
	 * 生成纯数字随机码，长度限制：[8, 18]
	 * 
	 * @param seq
	 * @param codeLength
	 * @return
	 */
	public static String genChar10(long seq, int codeLength) {
		if (codeLength < 8 || codeLength > 18) {
			throw new RuntimeException("Invalid code length, recommend: 8, 10, 12, 14, 16, 18.");
		}
		long maxSeq = (long) ((Math.pow(10, codeLength - 1) - SLAT) / PRIME1);
		if (seq <= 0 || seq > maxSeq) {
			throw new RuntimeException("Maximum seq value exceeded, max = " + maxSeq + ", seq = " + seq);
		}
		return encode(seq, codeLength, CHARS_10);
	}

	/**
	 * 生成随机码
	 *
	 * @param seq 支持的最大值为: (32^(codeLength-1) - {@link #SLAT}) / {@link #PRIME1}
	 * @return code
	 */
	private static String encode(long seq, int codeLength, char[] chars) {
		int CHARS_LENGTH = chars.length;
		// 补位
		seq = seq * PRIME1 + SLAT;
		// 将 id 转换成32进制的值
		long[] b = new long[codeLength];
		// 32进制数
		b[0] = seq;
		for (int i = 0; i < codeLength - 1; i++) {
			b[i + 1] = b[i] / CHARS_LENGTH;
			// 按位扩散
			b[i] = (b[i] + i * b[0]) % CHARS_LENGTH;
		}

		long tmp = 0;
		for (int i = 0; i < codeLength - 2; i++) {
			tmp += b[i];
		}
		b[codeLength - 1] = tmp * PRIME1 % CHARS_LENGTH;

		// 进行混淆
		long[] codeIndexArray = new long[codeLength];
		for (int i = 0; i < codeLength; i++) {
			codeIndexArray[i] = b[i * PRIME2 % codeLength];
		}

		StringBuilder buffer = new StringBuilder();
		Arrays.stream(codeIndexArray).boxed().map(Long::intValue).map(t -> chars[t]).forEach(buffer::append);
		return buffer.toString();
	}

	public static Long decodeChar10(String code) {
		return decode(code, CHARS_10);
	}

	public static Long decodeChar32(String code) {
		return decode(code, CHARS_32);
	}

	public static Long decodeChar56(String code) {
		return decode(code, CHARS_56);
	}

	/**
	 * 将随机码解密成原来的id
	 *
	 * @param code 激活码
	 * @return id
	 */
	private static Long decode(String code, char[] chars) {
		int codeLength = code.length();
		int CHARS_LENGTH = chars.length;
		// 将字符还原成对应数字
		long[] a = new long[codeLength];
		for (int i = 0; i < codeLength; i++) {
			char c = code.charAt(i);
			int index = findIndex(c, chars);
			if (index == -1) {
				// 异常字符串
				return null;
			}
			a[i * PRIME2 % codeLength] = index;
		}

		long[] b = new long[codeLength];
		for (int i = codeLength - 2; i >= 0; i--) {
			b[i] = (a[i] - a[0] * i + CHARS_LENGTH * i) % CHARS_LENGTH;
		}

		long res = 0;
		for (int i = codeLength - 2; i >= 0; i--) {
			res += b[i];
			res *= (i > 0 ? CHARS_LENGTH : 1);
		}
		return (res - SLAT) / PRIME1;
	}

	/**
	 * 查找对应字符的index
	 *
	 * @param c 字符
	 * @return index
	 */
	public static int findIndex(char c, char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		int charSize = CHARS_56.length;
		System.out.println(charSize);
		System.out.println(Integer.MAX_VALUE);
		System.out.println(Long.MAX_VALUE);
		System.out.println(((long) Math.pow(charSize, 1) - SLAT) / PRIME1); // 2位最大值
		System.out.println(((long) Math.pow(charSize, 3) - SLAT) / PRIME1); // 3位最大值
		System.out.println(((long) Math.pow(charSize, 5) - SLAT) / PRIME1); // 6位最大值
		System.out.println(((long) Math.pow(charSize, 7) - SLAT) / PRIME1); // 8位最大值，可覆盖Int
		System.out.println(((long) Math.pow(charSize, 9) - SLAT) / PRIME1); // 10位最大值
		System.out.println(((long) Math.pow(charSize, 11) - SLAT) / PRIME1); // 12位最大值
		System.out.println(((long) Math.pow(charSize, 13) - SLAT) / PRIME1); // 14位最大值
		System.out.println(((long) Math.pow(charSize, 15) - SLAT) / PRIME1); // 16位最大值
		System.out.println(((long) Math.pow(charSize, 17) - SLAT) / PRIME1); // 18位最大值
		System.out.println(((long) Math.pow(charSize, 19) - SLAT) / PRIME1); // 20位最大值，

		System.out.println("CHAR56>>>>>>>>>>>>");
		for (int i = 0; i < 10; i++) {
			long v = RandomUtils.nextInt();
			String genChar = genChar56(v, 10);
			System.out.println(v + " | " + genChar + " | " + decodeChar56(genChar));
		}
		System.out.println("CHAR56<<<<<<<<<<<<");

		System.out.println("CHAR32>>>>>>>>>>>>");
		for (int i = 0; i < 10; i++) {
			long v = RandomUtils.nextInt();
			String genChar = genChar32(v, 8);
			System.out.println(v + " | " + genChar + " | " + decodeChar32(genChar));
		}
		System.out.println("CHAR32<<<<<<<<<<<<");

		System.out.println("CHAR10>>>>>>>>>>>>");
		for (int i = 0; i < 10; i++) {
			long v = RandomUtils.nextInt();
			String genChar = genChar10(v, 14);
			System.out.println(v + " | " + genChar + " | " + decodeChar10(genChar));
		}
		System.out.println("CHAR10<<<<<<<<<<<<");

//		Set<String> set1 = new HashSet<>();
//		Set<String> set2 = new HashSet<>();
//		int max = (int) (((long) Math.pow(32, 5) - SLAT) / PRIME1);
//		for (int i = 0; i < 100000; i++) {
//			set1.add(String.valueOf(i));
//			String randomCode = encode(i, 6);
//			set2.add(randomCode);
//		}
//		System.out.println(set1.size());
//		System.out.println(set2.size());
	}
}