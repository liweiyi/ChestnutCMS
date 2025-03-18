/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.AntPathMatcher;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

	public static final String DOT = ".";

	/** 下划线 */
	public static final char C_Underline = '_';

	public static final String Underline = "" + C_Underline;

	/**
	 * 字符常量：反斜杠 {@code "/"}
	 */
	public static final char C_BACKSLASH = '\\';

	/**
	 * 字符串常量：反斜杠 {@code "\"}
	 */
	public static final String BACKSLASH = "" + C_BACKSLASH;

	/**
	 * 字符常量：斜杠 {@code "/"}
	 */
	public static final char C_SLASH = '/';

	/**
	 * 字符串常量：斜杠 {@code "/"}
	 */
	public static final String SLASH = "" + C_SLASH;

	/**
	 * 字符常量：逗号 {@code ','}
	 */
	public static final char C_COMMA = ',';

	/**
	 * 字符串常量：逗号 {@code ','}
	 */
	public static final String COMMA = "" + C_COMMA;

	/**
	 * 获取参数不为空值
	 * 
	 * @param value
	 *            defaultValue 要判断的value
	 * @return value 返回值
	 */
	public static <T> T nvl(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	/**
	 * * 判断一个Collection是否为空， 包含List，Set，Queue
	 * 
	 * @param coll
	 *            要判断的Collection
	 * @return true：为空 false：非空
	 */
	public static boolean isEmpty(Collection<?> coll) {
		return Objects.isNull(coll) || coll.isEmpty();
	}

	/**
	 * * 判断一个Collection是否非空，包含List，Set，Queue
	 * 
	 * @param coll
	 *            要判断的Collection
	 * @return true：非空 false：空
	 */
	public static boolean isNotEmpty(Collection<?> coll) {
		return !isEmpty(coll);
	}

	/**
	 * * 判断一个对象数组是否为空
	 * 
	 * @param objects
	 *            要判断的对象数组
	 ** @return true：为空 false：非空
	 */
	public static boolean isEmpty(Object[] objects) {
		return Objects.isNull(objects) || (objects.length == 0);
	}

	/**
	 * * 判断一个对象数组是否非空
	 * 
	 * @param objects
	 *            要判断的对象数组
	 * @return true：非空 false：空
	 */
	public static boolean isNotEmpty(Object[] objects) {
		return !isEmpty(objects);
	}

	/**
	 * * 判断一个Map是否为空
	 * 
	 * @param map
	 *            要判断的Map
	 * @return true：为空 false：非空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return Objects.isNull(map) || map.isEmpty();
	}

	/**
	 * * 判断一个Map是否为空
	 * 
	 * @param map
	 *            要判断的Map
	 * @return true：非空 false：空
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * * 判断一个字符串是否为空串
	 * 
	 * @param str
	 *            String
	 * @return true：为空 false：非空
	 */
	public static boolean isEmpty(String str) {
		return Objects.isNull(str) || EMPTY.equals(str.trim());
	}

	/**
	 * * 判断一个字符串是否为非空串
	 * 
	 * @param str
	 *            String
	 * @return true：非空串 false：空串
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static void ifNotEmpty(String str, Consumer<String> consumer) {
		if (!isEmpty(str)) {
			consumer.accept(str);
		}
	}

	/**
	 * * 判断一个对象是否是数组类型（Java基本型别的数组）
	 * 
	 * @param object
	 *            对象
	 * @return true：是数组 false：不是数组
	 */
	public static boolean isArray(Object object) {
		return Objects.nonNull(object) && object.getClass().isArray();
	}

	/**
	 * 去空格
	 */
	public static String trim(String str) {
		return (str == null ? "" : str.trim());
	}

	public static String firstNotBlankStr(String... strArr) {
		return filterFirst(StringUtils::isNotBlank, strArr);
	}

	/**
	 * 获取符合条件的第一个字符串
	 *
	 * @param predicate
	 * @param strArr
	 * @return
	 */
	public static String filterFirst(Predicate<String> predicate, String... strArr) {
		if (isEmpty(strArr)) {
			return null;
		}
		for (int i = 0; i < strArr.length; i++) {
			if (predicate.test(strArr[i])) {
				return strArr[i];
			}
		}
		return null;
	}

	/**
	 * 截取字符串
	 * 
	 * @param str
	 *            字符串
	 * @param start
	 *            开始
	 * @return 结果
	 */
	public static String substring(final String str, int start) {
		if (str == null) {
			return EMPTY;
		}

		if (start < 0) {
			start = str.length() + start;
		}

		if (start < 0) {
			start = 0;
		}
		if (start > str.length()) {
			return EMPTY;
		}

		return str.substring(start);
	}

	/**
	 * 截取字符串
	 * 
	 * @param str
	 *            字符串
	 * @param start
	 *            开始
	 * @param end
	 *            结束
	 * @return 结果
	 */
	public static String substring(final String str, int start, int end) {
		if (str == null) {
			return EMPTY;
		}

		if (end < 0) {
			end = str.length() + end;
		}
		if (start < 0) {
			start = str.length() + start;
		}

		if (end > str.length()) {
			end = str.length();
		}

		if (start > end) {
			return EMPTY;
		}

		if (start < 0) {
			start = 0;
		}
		if (end < 0) {
			end = 0;
		}

		return str.substring(start, end);
	}

	/**
	 * 字符串转set
	 * 
	 * @param str
	 *            字符串
	 * @param sep
	 *            分隔符
	 * @return set集合
	 */
	public static final Set<String> str2Set(String str, String sep) {
		return new HashSet<String>(str2List(str, sep, true, false));
	}

	/**
	 * 字符串转list
	 * 
	 * @param str
	 *            字符串
	 * @param sep
	 *            分隔符
	 * @param filterBlank
	 *            过滤纯空白
	 * @param trim
	 *            去掉首尾空白
	 * @return list集合
	 */
	public static final List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
		List<String> list = new ArrayList<String>();
		if (StringUtils.isEmpty(str)) {
			return list;
		}

		// 过滤空白字符串
		if (filterBlank && StringUtils.isBlank(str)) {
			return list;
		}
		String[] split = str.split(sep);
		for (String string : split) {
			if (filterBlank && StringUtils.isBlank(string)) {
				continue;
			}
			if (trim) {
				string = string.trim();
			}
			list.add(string);
		}

		return list;
	}
	
	/**
	 * 判断给定的set列表中是否包含数组array 判断给定的数组array中是否包含给定的元素value
	 *
	 * @param collection
	 *            给定的集合
	 * @param array
	 *            给定的数组
	 * @return boolean 结果
	 */
	public static boolean containsAny(Collection<String> collection, String... array) {
		if (isEmpty(collection) || isEmpty(array)) {
			return false;
		} else {
			for (String str : array) {
				if (collection.contains(str)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 查找指定字符串是否包含指定字符串列表中的任意一个字符串同时串忽略大小写
	 *
	 * @param cs
	 *            指定字符串
	 * @param searchCharSequences
	 *            需要检查的字符串数组
	 * @return 是否包含任意一个字符串
	 */
	public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences) {
		if (isEmpty(cs) || isEmpty(searchCharSequences)) {
			return false;
		}
		for (CharSequence testStr : searchCharSequences) {
			if (containsIgnoreCase(cs, testStr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 驼峰转下划线命名
	 */
	public static String toUnderScoreCase(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		// 前置字符是否大写
		boolean preCharIsUpperCase = true;
		// 当前字符是否大写
		boolean curreCharIsUpperCase = true;
		// 下一字符是否大写
		boolean nexteCharIsUpperCase = true;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (i > 0) {
				preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
			} else {
				preCharIsUpperCase = false;
			}

			curreCharIsUpperCase = Character.isUpperCase(c);

			if (i < (str.length() - 1)) {
				nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
			}

			if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
				sb.append(Underline);
			} else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
				sb.append(Underline);
			}
			sb.append(Character.toLowerCase(c));
		}

		return sb.toString();
	}

	/**
	 * 是否包含字符串
	 * 
	 * @param str
	 *            验证字符串
	 * @param strs
	 *            字符串组
	 * @return 包含返回true
	 */
	public static boolean inStringIgnoreCase(String str, String... strs) {
		if (str != null && strs != null) {
			for (String s : strs) {
				if (str.equalsIgnoreCase(trim(s))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
	 * 例如：HELLO_WORLD->HelloWorld
	 * 
	 * @param name
	 *            转换前的下划线大写方式命名的字符串
	 * @return 转换后的驼峰式命名的字符串
	 */
	public static String convertToCamelCase(String name) {
		StringBuilder result = new StringBuilder();
		// 快速检查
		if (name == null || name.isEmpty()) {
			// 没必要转换
			return "";
		} else if (!name.contains("_")) {
			// 不含下划线，仅将首字母大写
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		// 用下划线将原始字符串分割
		String[] camels = name.split("_");
		for (String camel : camels) {
			// 跳过原始字符串中开头、结尾的下换线或双重下划线
			if (camel.isEmpty()) {
				continue;
			}
			// 首字母大写
			result.append(camel.substring(0, 1).toUpperCase());
			result.append(camel.substring(1).toLowerCase());
		}
		return result.toString();
	}

	/**
	 * 驼峰式命名法 例如：user_name->userName
	 */
	public static String toCamelCase(String s) {
		if (s == null) {
			return null;
		}
		s = s.toLowerCase();
		StringBuilder sb = new StringBuilder(s.length());
		boolean upperCase = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == C_Underline) {
				upperCase = true;
			} else if (upperCase) {
				sb.append(Character.toUpperCase(c));
				upperCase = false;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
	 * 
	 * @param str
	 *            指定字符串
	 * @param strs
	 *            需要检查的字符串数组
	 * @return 是否匹配
	 */
	public static boolean matches(String str, List<String> strs) {
		if (isEmpty(str) || isEmpty(strs)) {
			return false;
		}
		for (String pattern : strs) {
			if (isMatch(pattern, str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断url是否与规则配置: ? 表示单个字符; * 表示一层路径内的任意字符串，不可跨层级; ** 表示任意层路径;
	 * 
	 * @param pattern
	 *            匹配规则
	 * @param url
	 *            需要匹配的url
	 * @return
	 */
	public static boolean isMatch(String pattern, String url) {
		AntPathMatcher matcher = new AntPathMatcher();
		return matcher.match(pattern, url);
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj) {
		return (T) obj;
	}

	/**
	 * 数字左边补齐0，使之达到指定长度。注意，如果数字转换为字符串后，长度大于size，则只保留 最后size个字符。
	 * 
	 * @param num
	 *            数字对象
	 * @param size
	 *            字符串指定长度
	 * @return 返回数字的字符串格式，该字符串为指定长度。
	 */
	public static final String padl(final Number num, final int size) {
		return padl(num.toString(), size, '0');
	}

	/**
	 * 字符串左补齐。如果原始字符串s长度大于size，则只保留最后size个字符。
	 * 
	 * @param s
	 *            原始字符串
	 * @param size
	 *            字符串指定长度
	 * @param c
	 *            用于补齐的字符
	 * @return 返回指定长度的字符串，由原字符串左补齐或截取得到。
	 */
	public static final String padl(final String s, final int size, final char c) {
		final StringBuilder sb = new StringBuilder(size);
		if (s != null) {
			final int len = s.length();
			if (s.length() <= size) {
				for (int i = size - len; i > 0; i--) {
					sb.append(c);
				}
				sb.append(s);
			} else {
				return s.substring(len - size, len);
			}
		} else {
			for (int i = size; i > 0; i--) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 字符串转Map
	 * 
	 * @param str
	 * @param entrySpliter
	 * @param keySpliter
	 * @return
	 */
	public static Map<String, String> splitToMap(String str, String entrySpliter, String keySpliter) {
		Map<String, String> map = new HashMap<>();
		if (isEmpty(str)) {
			return map;
		}
		String[] entries = str.split(entrySpliter);
		for (String entry : entries) {
			String[] kv = entry.split(keySpliter);
			String key = kv[0].trim();
			if (isEmpty(key)) {
				continue;
			}
			map.put(key, kv.length > 1 ? kv[1] : null);
		}
		return map;
	}

	public static String mapToString(Map<String, Object> map, String entrySpliter, String keySpliter) {
		if (Objects.isNull(map) || map.isEmpty()) {
			return EMPTY;
		}
		return map.entrySet().stream().map(e -> e.getKey() + keySpliter + (Objects.isNull(e.getValue()) ? EMPTY : e.getValue().toString()))
				.collect(Collectors.joining(entrySpliter));
	}

	/**
	 * 分隔字符串未字符串数据，并且忽略空字符串
	 * 
	 * @param str
	 * @param separator
	 * @return
	 */
	public static String[] splitIgnoreEmpty(String str, String separator) {
		if (StringUtils.isBlank(str)) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
        final int len = str.length();
		final int separatorLength = separator.length();
        
        final ArrayList<String> substrings = new ArrayList<>();
        int beg = 0;
        int end = 0;
        while (end < len) {
            end = str.indexOf(separator, beg);

            if (end > -1) {
                if (end > beg) {
                	String substr = str.substring(beg, end);
                	if (substr.length() > 0) {
                		substrings.add(substr);
                	}
                    beg = end + separatorLength;
                } else {
                    beg = end + separatorLength;
                }
            } else {
            	String substr = str.substring(beg);
            	if (substr.length() > 0) {
            		substrings.add(substr);
            	}
                end = len;
            }
        }

        return substrings.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
	}
	
	/**
	 * MessageFormat.format
	 * 
	 * @param message
	 * @param params
	 * @return
	 */
	public static String messageFormat(String message, Object... params) {
		if (isBlank(message) || isEmpty(params)) {
			return message;
		}
		return MessageFormat.format(message, params);
	}

	/**
	 * 首字符转大写
	 * 
	 * @param str
	 * @return
	 */
	public static String upperFirst(String str) {
		if (isEmpty(str)) {
			return str;
		}
		char firstChar = str.charAt(0);
		if (Character.isLowerCase(firstChar)) {
			return Character.toUpperCase(firstChar) + str.substring(1);
		}
		return str;
	}

	/**
	 * 首字符转小写
	 * 
	 * @param str
	 * @return
	 */
	public static String lowerFirst(String str) {
		if (isEmpty(str)) {
			return str;
		}
		char firstChar = str.charAt(0);
		if (Character.isUpperCase(firstChar)) {
			return Character.toLowerCase(firstChar) + str.substring(1);
		}
		return str;
	}
	
	/**
	 * 将一个字符串中的指定片段全部替换，替换过程中不进行正则处理。<br>
	 * 
	 * @param str
	 * @param searchStr
	 * @param replacement
	 * @return
	 */
	public static String replaceEx(String str, String searchStr, String replacement) {
		if (str == null || str.length() == 0 || replacement == null) {
			return str;
		}
		if (searchStr == null || searchStr.length() == 0 || searchStr.length() > str.length()) {
			return str;
		}
		StringBuilder sb = null;
		int lastIndex = 0;
		while (true) {
			int index = str.indexOf(searchStr, lastIndex);
			if (index < 0) {
				break;
			} else {
				if (sb == null) {
					sb = new StringBuilder();
				}
				sb.append(str.substring(lastIndex, index));
				sb.append(replacement);
			}
			lastIndex = index + searchStr.length();
		}
		if (lastIndex == 0) {
			return str;
		}
		sb.append(str.substring(lastIndex));
		return sb.toString();
	}

    public static String nullToEmpty(String str) {
		if (str == null) {
			return EMPTY;
		}
		return str;
    }

	/**
	 * 返回url路径后的参数集合
	 *
	 * @param path
	 * @return
	 */
    public static Map<String, String> getPathParameterMap(String path) {
		String str = substringAfter(path, "?");
		return splitToMap(str, "&", "=");
    }

	/**
	 * 转二进制字符串
	 *
	 * @param str 字符串
	 * @param leftPad 高位补零
	 */
	public static String toBinary(String str, boolean leftPad) {
		StringBuilder binary = new StringBuilder();
		for (char ch : str.toCharArray()) {
			String binaryString = Integer.toBinaryString(ch);
			if (leftPad) {
				binaryString = leftPad(binaryString, 8, '0');
			}
			binary.append(binaryString);
		}
		return binary.toString();
	}

	public static String toBinary(String str) {
		return toBinary(str, true);
	}

	public static String appendIfNotEmpty(String str, String value, String appendStr) {
		if (isNotEmpty(value)) {
			str += appendStr;
		}
		return str;
	}
}