package com.chestnut.common.utils;

import java.math.BigDecimal;
import java.util.Objects;

public class ConvertUtils {
	
	/**
	 * 字符串转整数基础类型
	 * 
	 * @param str
	 * @return
	 */
	public static int toInt(String str) {
		return toInt(str, 0);
	}
	
	/**
	 * 字符串转整数基础类型
	 * 
	 * @param str
	 * @return
	 */
	public static int toInt(String str, int defaultV) {
		return NumberUtils.toInt(str, defaultV);
	}
	
	public static int toInt(Object obj) {
		return toInt(obj, 0);
	}
	
	public static int toInt(Object obj, int defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return toInt(obj.toString(), defaultV);
	}
	
	public static Integer toInteger(String str) {
		return toInteger(str, 0);
	}

	/**
	 * 字符串转整数类型，如果非数字则返回默认值或0
	 * 
	 * @param str
	 * @param defaultV
	 * @return
	 */
	public static Integer toInteger(String str, Integer defaultV) {
		return Integer.valueOf(toInt(str, defaultV));
	}
	
	/**
	 * 字符串转布尔基础类型
	 * 
	 * @param str
	 * @return
	 */
	public static boolean toBoolean(String str) {
		return toBoolean(str, false);
	}
	
	/**
	 * 字符串转布尔类型
	 * "true" || "1" -> true
	 * "false" || "0" -> false
	 * 
	 * @param str
	 * @param defaultV
	 * @return
	 */
	public static Boolean toBoolean(String str, boolean defaultV) {
		return switch(str) {
			case "true" -> Boolean.TRUE;
			case "false" -> Boolean.FALSE;
			case "1" -> Boolean.TRUE;
			case "0" -> Boolean.FALSE;
			default -> defaultV;
		};
	}
	
	public static Boolean toBoolean(Object obj, boolean defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return toBoolean(obj.toString(), defaultV);
	}

	public static long toLong(Object obj) {
		return toLong(obj, 0);
	}

	/**
	 * 字符串转长整数类型，如果非数字则返回默认值或0
	 * 
	 * @param obj
	 * @param defaultV
	 * @return
	 */
	public static long toLong(Object obj, long defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return NumberUtils.toLong(obj.toString(), defaultV);
	}

	public static float toFloat(Object obj) {
		return toFloat(obj, 0);
	}

	/**
	 * 字符串转浮点类型，如果非数字则返回默认值或0
	 * 
	 * @param obj
	 * @param defaultV
	 * @return
	 */
	public static float toFloat(Object obj, float defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return NumberUtils.toFloat(obj.toString(), defaultV);
	}

	public static double toDouble(Object obj) {
		return toDouble(obj, 0);
	}

	/**
	 * 字符串转双精度浮点类型，如果非数字则返回默认值或0
	 * 
	 * @param obj
	 * @param defaultV
	 * @return
	 */
	public static double toDouble(Object obj, double defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return NumberUtils.toDouble(obj.toString(), defaultV);
	}

	public static byte toByte(Object obj) {
		return toByte(obj, (byte) 0);
	}

	/**
	 * 字符串转字节类型，如果非数字则返回默认值或0
	 * 
	 * @param obj
	 * @param defaultV
	 * @return
	 */
	public static byte toByte(Object obj, byte defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return NumberUtils.toByte(obj.toString(), defaultV);
	}

	public static short toShort(Object obj) {
		return toShort(obj, (short) 0);
	}

	/**
	 * 字符串转短整数类型，如果非数字则返回默认值或0
	 * 
	 * @param obj
	 * @param defaultV
	 * @return
	 */
	public static short toShort(Object obj, short defaultV) {
		if (Objects.isNull(obj)) {
			return defaultV;
		}
		return NumberUtils.toShort(obj.toString(), defaultV);
	}
	
	public static BigDecimal toBigDecimal(Object obj) {
		return toBigDecimal(obj, BigDecimal.ZERO);
	}
	
	public static BigDecimal toBigDecimal(Object obj, BigDecimal defaultV) {
		if (Objects.isNull(obj)) {
			return BigDecimal.ZERO;
		}
		return NumberUtils.toScaledBigDecimal(obj.toString());
	}

	/**
	 * 字符串如果为null则返回默认值
	 * 
	 * @param str
	 * @param defaultValue
	 * @return
	 */
	public static String toStr(Object str, String defaultValue) {
		return Objects.isNull(str) ? defaultValue : str.toString();
	}

	public static String toStr(Object str) {
		return toStr(str, null);
	}
}
