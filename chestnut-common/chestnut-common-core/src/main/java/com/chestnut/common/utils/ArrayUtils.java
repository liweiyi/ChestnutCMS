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

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ArrayUtils {

    public static Set<String> union(Collection<Set<String>> colArray) {
        Set<String> set = new HashSet<>();
        if (colArray == null) {
            return set;
        }
        for (Set<String> strings : colArray) {
            set.addAll(strings);
        }
        return set;
    }

    public static boolean contains(String str, Collection<String> searchStrings, boolean ignoreCase) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(searchStrings)) {
            return false;
        }
        for (String testStr : searchStrings) {
            if (ignoreCase) {
                if (str.equalsIgnoreCase(testStr)) {
                    return true;
                }
            } else {
                if (str.equals( testStr)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsIgnoreCase(String str, Collection<String> searchStrings) {
        return contains(str, searchStrings, true);
    }

    public static boolean contains(String str, Collection<String> searchStrings) {
        return contains(str, searchStrings, false);
    }

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

	public static boolean contains(String searchStr, String... arr) {
		return indexOf(searchStr, arr) > -1;
	}

	public static int indexOf(Integer searchStr, Integer... arr) {
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

	public static boolean contains(Integer searchStr, Integer... arr) {
		return indexOf(searchStr, arr) > -1;
	}

	/**
	 * 查找指定列表中符合条件的第一个元素并返回，如果没有符合条件的元素直接抛出异常
	 */
	public static <T> T first(Collection<T> list, Predicate<T> predicate) {
		if (Objects.nonNull(list)) {
			for (T item : list) {
				if (predicate.test(item)) {
					return item;
				}
			}
		}
		throw new NullPointerException("No matched item in list.");
	}

	/**
	 * 查找指定列表中符合条件的第一个元素并返回，如果没有符合条件的元素返回NULL
	 */
	public static <T> T firstOrNull(Collection<T> list, Predicate<T> predicate) {
		if (Objects.nonNull(list)) {
			for (T item : list) {
				if (predicate.test(item)) {
					return item;
				}
			}
		}
		return null;
	}

	public static <T> T firstOrElse(Collection<T> list, T defaultV) {
		if (StringUtils.isNotEmpty(list)) {
			return list.iterator().next();
		}
		return defaultV;
	}

	public static Map<String, List<Map<String, ?>>> groupBy(List<Map<String, ?>> list, String groupBy) {
		Map<String, List<Map<String, ?>>> map = new HashMap<>();
		list.forEach(obj -> {
			String key = obj.get(groupBy).toString();
			List<Map<String, ?>> groupList = map.get(key);
			if (Objects.isNull(groupList)) {
				groupList = new ArrayList<>();
				map.put(key, groupList);
			}
			groupList.add(obj);
		});
		return map;
	}

	public static <T> Map<String, List<T>> groupBy(List<T> list, Function<T, String> getter) {
		Map<String, List<T>> map = new HashMap<>();
		list.forEach(obj -> {
			String key = getter.apply(obj);
			List<T> groupList = map.get(key);
			if (Objects.isNull(groupList)) {
				groupList = new ArrayList<>();
				map.put(key, groupList);
			}
			groupList.add(obj);
		});
		return map;
	}

	public static <T, R> List<R> map(T[] array, Function<T, R> mapper) {
		if (Objects.isNull(array)) {
			return List.of();
		}
		return Stream.of(array).map(mapper).toList();
	}

	public static <T, R> List<R> mapNotNull(T[] array, Function<T, R> mapper) {
		if (Objects.isNull(array)) {
			return List.of();
		}
		return Stream.of(array).map(mapper).filter(Objects::nonNull).toList();
	}

	public static <T, R> List<R> mapNotNull(List<T> list, Function<T, R> mapper) {
		if (Objects.isNull(list)) {
			return List.of();
		}
		return list.stream().map(mapper).filter(Objects::nonNull).toList();
	}

    public static <T> boolean isEmpty(T[] arr) {
		return Objects.isNull(arr) || arr.length == 0;
    }

	public static <T> boolean isNotEmpty(T[] arr) {
		return !isEmpty(arr);
	}

	public static <T> long sumLongValue(List<T> list, Function<T, Long> getter) {
		if (Objects.isNull(list)) {
			return 0;
		}
		long sum = 0L;
		for (T t : list) {
			Long v = getter.apply(t);
			if (Objects.nonNull(v)) {
				sum += v;
			}
		}
		return sum;
	}

	public static <T> int sumIntValue(List<T> list, Function<T, Integer> getter) {
		if (Objects.isNull(list)) {
			return 0;
		}
		int sum = 0;
		for (T t : list) {
			Integer v = getter.apply(t);
			sum += Objects.requireNonNullElse(v, 0);
		}
		return sum;
	}
}
