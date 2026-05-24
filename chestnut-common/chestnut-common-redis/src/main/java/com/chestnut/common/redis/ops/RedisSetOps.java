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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Set 操作组件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisSetOps extends AbstractRedisOps {

    public RedisSetOps(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties cacheProperties) {
        super(redisTemplate, cacheProperties);
    }

    /**
     * 写入 Set（无 TTL）。
     */
    public <T> void setCacheSet(String key, Set<T> dataSet) {
        setCacheSet(key, dataSet, -1, null);
    }

    /**
     * 写入 Set 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> void setCacheSet(String key, Set<T> dataSet, long ttl, TimeUnit timeUnit) {
        if (StringUtils.isEmpty(dataSet)) {
            return;
        }
        redisTemplate.opsForSet().add(key, dataSet.toArray(Object[]::new));
        if (ttl > 0 && timeUnit != null) {
            redisTemplate.expire(key, jitter(timeUnit.toSeconds(ttl)), TimeUnit.SECONDS);
        }
    }

    public <T> Set<T> getCacheSet(String key, Class<T> clazz) {
        Set<Object> cacheValue = redisTemplate.opsForSet().members(key);
        if (cacheValue == null || cacheValue.isEmpty()) {
            return Set.of();
        }
        Set<T> result = new HashSet<>(cacheValue.size());
        for (Object obj : cacheValue) {
            result.add(clazz.cast(obj));
        }
        return result;
    }

    public Long getCacheSetSize(String key) {
        return Objects.requireNonNullElse(redisTemplate.opsForSet().size(key), 0L);
    }

    public void addSetValue(String key, Object... values) {
        if (StringUtils.isEmpty(values)) {
            return;
        }
        redisTemplate.opsForSet().add(key, values);
    }

    public void removeSetValue(String key, Object... values) {
        if (StringUtils.isEmpty(values)) {
            return;
        }
        redisTemplate.opsForSet().remove(key, values);
    }

    public boolean isSetContains(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public boolean isSetContainsAny(String key, Object... values) {
        Map<Object, Boolean> map = redisTemplate.opsForSet().isMember(key, values);
        if (map == null) {
            return false;
        }
        return map.values().stream().anyMatch(Boolean.TRUE::equals);
    }

    public <T> T randomSetValue(String key, Class<T> clazz) {
        if (!redisTemplate.hasKey(key)) {
            return null;
        }
        Object value = redisTemplate.opsForSet().randomMember(key);
        return clazz.cast(value);
    }

    public <T> List<T> randomSetValues(String key, long count, Class<T> clazz) {
        if (!redisTemplate.hasKey(key)) {
            return List.of();
        }
        List<Object> objects = redisTemplate.opsForSet().randomMembers(key, count);
        if (objects == null || objects.isEmpty()) {
            return List.of();
        }
        return objects.stream().map(clazz::cast).toList();
    }
}
