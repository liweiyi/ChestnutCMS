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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Supplier;

public class  ConvertUtils {

	/**
	 * 对象Object转指定格式
	 *
	 * @param obj 对象
	 * @param nonNullSupplier 非空转换器
	 * @param nullSupplier 空值转换器
	 */
	public static <T> T nonNullOrElse(Object obj, Supplier<T> nonNullSupplier, Supplier<T> nullSupplier) {
		if (Objects.isNull(obj)) {
			return nullSupplier.get();
		}
		return nonNullSupplier.get();
	}

	public static <T> T nonNull(Object obj, Supplier<T> nonNullSupplier) {
		if (Objects.isNull(obj)) {
			return null;
		}
		return nonNullSupplier.get();
	}

	/**
	 * 转整数类型，默认返回null
	 */
	public static Integer toInteger(Object obj) {
		return toInteger(obj, null);
	}

	/**
	 * 转整数类型
	 */
	public static Integer toInteger(Object obj, Integer defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return Integer.valueOf(obj.toString());
			} catch (NumberFormatException e) {
				return defaultV;
			}
		}, () -> defaultV);
	}

	/**
	 * 字符串转布尔类型
	 * "true" || "1" -> true
	 * "false" || "0" -> false
	 */
	private static Boolean paseBoolean(String str, boolean defaultV) {
		return switch(str) {
			case "true", "1" -> Boolean.TRUE;
			case "false", "0" -> Boolean.FALSE;
			default -> defaultV;
		};
	}

	/**
	 * 转布尔类型，默认返回false
	 */
	public static Boolean toBoolean(Object obj) {
		return toBoolean(obj, null);
	}

	public static Boolean toBoolean(Object obj, Boolean defaultV) {
		return nonNullOrElse(obj, () -> paseBoolean(obj.toString(), defaultV), () -> defaultV);
	}

	/**
	 * 转长整数类型，默认返回null
	 */
	public static Long toLong(Object obj) {
		return toLong(obj, null);
	}

	/**
	 * 转长整数类型
	 */
	public static Long toLong(Object obj, Long defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return Long.parseLong(obj.toString());
			} catch (final NumberFormatException nfe) {
				return defaultV;
			}
		}, () -> defaultV);
	}

	/**
	 * 转浮点类型，默认返回null
	 */
	public static Float toFloat(Object obj) {
		return toFloat(obj, null);
	}

	/**
	 * 转浮点类型
	 */
	public static Float toFloat(Object obj, Float defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return Float.parseFloat(obj.toString());
			} catch (final NumberFormatException nfe) {
				return defaultV;
			}
		}, () -> defaultV);
	}

	/**
	 * 转双精度浮点类型，默认返回null
	 */
	public static Double toDouble(Object obj) {
		return toDouble(obj, null);
	}

	/**
	 * 转双精度浮点类型
	 */
	public static Double toDouble(Object obj, Double defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return Double.parseDouble(obj.toString());
			} catch (final NumberFormatException nfe) {
				return defaultV;
			}
		}, () -> defaultV);
	}

	/**
	 * 转字节类型，默认返回null
	 */
	public static Byte toByte(Object obj) {
		return toByte(obj, null);
	}

	/**
	 * 转字节类型
	 */
	public static Byte toByte(Object obj, Byte defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return Byte.parseByte(obj.toString());
			} catch (final NumberFormatException nfe) {
				return defaultV;
			}
		}, () -> defaultV);
	}

	/**
	 * 转短整数类型，默认值null
	 */
	public static Short toShort(Object obj) {
		return toShort(obj, null);
	}

	/**
	 * 转短整数类型
	 */
	public static Short toShort(Object obj, Short defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return Short.parseShort(obj.toString());
			} catch (final NumberFormatException nfe) {
				return defaultV;
			}
		}, () -> defaultV);
	}

	/**
	 * 转BigDecimal，默认BigDecimal.ZERO
	 */
	public static BigDecimal toBigDecimal(Object obj) {
		return toBigDecimal(obj, BigDecimal.ZERO);
	}

	/**
	 * 转BigDecimal
	 */
	public static BigDecimal toBigDecimal(Object obj, BigDecimal defaultV) {
		return nonNullOrElse(obj, () -> NumberUtils.toScaledBigDecimal(obj.toString()), () -> defaultV);
	}

	/**
	 * 字符串如果为null则返回默认值
	 */
	public static String toStr(Object obj, String defaultValue) {
		return Objects.isNull(obj) ? defaultValue : obj.toString();
	}

	public static String toStr(Object obj) {
		return toStr(obj, null);
	}


	public static LocalDateTime toLocalDateTime(Object obj) {
		return toLocalDateTime(obj, null);
	}

	public static LocalDateTime toLocalDateTime(Object obj, LocalDateTime defaultV) {
		return nonNullOrElse(obj, () -> {
			try {
				return LocalDateTime.parse(obj.toString());
			} catch (DateTimeParseException e) {
				return defaultV;
			}
		}, () -> defaultV);
	}
}
