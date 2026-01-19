/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * XCollectionUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class XCollectionUtils {

    /**
     * Creates a string from all the elements separated using [separator].
     */
    public static <T> String join(Collection<T> collection, String separator, Function<T, String> func) {
        return join(collection, separator, func);
    }

    /**
     * Creates a string from all the elements separated using [separator].
     */
    public static <T> String join(Collection<T> collection, String separator, Function<T, String> func, Predicate<T> predicate) {
        StringBuilder sb = new StringBuilder();
        for (T item : collection) {
            if (predicate.test(item)) {
                if (!sb.isEmpty()) {
                    sb.append(separator);
                }
                sb.append(func.apply(item));
            }
        }
        return sb.toString();
    }

    /**
     * Returns a [ImmutableMap] containing key-value pairs provided by [keyGetter] function and valueGetter function
     * applied to elements of the given collection.
     *
     * If any of two pairs would have the same key the last one gets added to the map.
     *
     * The returned map preserves the entry iteration order of the original collection.
     */
    public static <T, K, V> Map<K, V> associate(Collection<T> collection, Function<T, K> keyGetter, Function<T, V> valueGetter) {
        Map<K, V> map = new HashMap<>(collection.size());
        for (T item : collection) {
            map.put(keyGetter.apply(item), valueGetter.apply(item));
        }
        return Collections.unmodifiableMap(map);
    }
}
