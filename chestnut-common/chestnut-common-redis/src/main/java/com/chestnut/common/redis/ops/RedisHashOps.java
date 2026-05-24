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
package com.chestnut.common.redis.ops;

import com.chestnut.common.redis.config.RedisCacheProperties;
import com.chestnut.common.utils.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis Hash 操作组件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisHashOps extends AbstractRedisOps {

    public RedisHashOps(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties cacheProperties) {
        super(redisTemplate, cacheProperties);
    }

    public <T> void setCacheMap(String key, Map<String, T> dataMap) {
        if (StringUtils.isNotEmpty(dataMap)) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 写入 Hash 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> void setCacheMap(String key, Map<String, T> dataMap, long ttl, TimeUnit timeUnit) {
        if (StringUtils.isNotEmpty(dataMap)) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
        if (ttl > 0 && timeUnit != null) {
            redisTemplate.expire(key, jitter(timeUnit.toSeconds(ttl)), TimeUnit.SECONDS);
        }
    }

    public <T> void setCacheMapValues(String key, Map<String, T> dataMap) {
        redisTemplate.opsForHash().putAll(key, dataMap);
    }

    public <T> Map<String, T> getCacheMap(String key, Class<T> clazz) {
        if (!redisTemplate.hasKey(key)) {
            return null;
        }
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (StringUtils.isEmpty(entries)) {
            return Map.of();
        }
        return toTypedMap(entries, clazz);
    }

    public <T> Map<String, T> getCacheMap(String key, Class<T> clazz, Supplier<Map<String, T>> supplier) {
        if (!redisTemplate.hasKey(key)) {
            Map<String, T> dataMap = Objects.requireNonNullElse(
                    supplier != null ? supplier.get() : null, Map.of());
            setCacheMap(key, dataMap);
            return dataMap;
        }
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return toTypedMap(entries, clazz);
    }

    /**
     * 获取 Hash 缓存，miss 时调用 supplier 回填并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> Map<String, T> getCacheMap(String key, Class<T> clazz, long ttl, TimeUnit timeUnit,
                                          Supplier<Map<String, T>> supplier) {
        if (!redisTemplate.hasKey(key)) {
            Map<String, T> dataMap = Objects.requireNonNullElse(
                    supplier != null ? supplier.get() : null, Map.of());
            setCacheMap(key, dataMap, ttl, timeUnit);
            return dataMap;
        }
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return toTypedMap(entries, clazz);
    }

    public <T> void setCacheMapValue(String key, String hKey, T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCacheMapValue(String key, String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getMultiCacheMapValue(String key, Collection<String> hKeys) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.multiGet(key, hKeys);
    }

    public boolean deleteCacheMapValue(String key, Object... hKeys) {
        if (StringUtils.isEmpty(hKeys)) {
            return false;
        }
        return redisTemplate.opsForHash().delete(key, hKeys) > 0;
    }

    public boolean hasMapKey(String cacheKey, String hashKey) {
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(cacheKey, hashKey));
    }

    public void incrMapValue(String cacheKey, String hKey, long delta) {
        redisTemplate.opsForHash().increment(cacheKey, hKey, delta);
    }

    private <T> Map<String, T> toTypedMap(Map<Object, Object> entries, Class<T> clazz) {
        Map<String, T> map = new HashMap<>(entries.size());
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            map.put(entry.getKey().toString(), clazz.cast(entry.getValue()));
        }
        return map;
    }
}
