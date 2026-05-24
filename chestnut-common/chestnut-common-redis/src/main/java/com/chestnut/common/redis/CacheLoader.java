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
package com.chestnut.common.redis;

/**
 * 缓存数据加载器，用于 {@code loadIfAbsent} 系列方法。
 *
 * <p>注意：当数据源发生异常时应<b>抛出异常</b>，而非返回 {@code null}。
 * 返回 {@code null} 表示"该数据在数据源中确实不存在"，
 * 工具类会将其短期缓存为占位（防穿透），而非每次都回源。
 *
 * @param <T> 缓存值类型
 * @author 兮玥
 * @email 190785909@qq.com
 */
@FunctionalInterface
public interface CacheLoader<T> {

    T load();
}
