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
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis ZSet（有序集合）操作组件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisZSetOps extends AbstractRedisOps {

    public RedisZSetOps(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties cacheProperties) {
        super(redisTemplate, cacheProperties);
    }

    public boolean addZset(String key, Object value, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
    }

    /**
     * 添加元素到 ZSet 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public boolean addZset(String key, Object value, double score, long ttl, TimeUnit timeUnit) {
        boolean result = Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        if (ttl > 0 && timeUnit != null) {
            redisTemplate.expire(key, jitter(timeUnit.toSeconds(ttl)), TimeUnit.SECONDS);
        }
        return result;
    }

    public long zsetIncr(String key, String value, double delta) {
        Double score = redisTemplate.opsForZSet().incrementScore(key, value, delta);
        return ConvertUtils.toLong(score, -1L);
    }

    public Double getZsetScore(String key, String value) {
        Double score = redisTemplate.opsForZSet().score(key, value);
        return Objects.requireNonNullElse(score, -1d);
    }

    public boolean isValueInZset(String key, Object value) {
        ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
        return Objects.nonNull(operations.score(key, value));
    }

    public void removeZsetValue(String key, Object... values) {
        if (StringUtils.isEmpty(values)) {
            return;
        }
        redisTemplate.opsForZSet().remove(key, values);
    }

    public void removeZsetValueByScore(String key, double min, double max) {
        redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    public long getZsetSize(String key) {
        return Objects.requireNonNullElse(redisTemplate.opsForZSet().size(key), 0L);
    }

    public <T> Set<T> getZset(String key, int start, int end, Class<T> clazz) {
        Set<Object> objects = redisTemplate.opsForZSet().range(key, start, end);
        if (objects == null || objects.isEmpty()) {
            return Set.of();
        }
        Set<T> result = new HashSet<>(objects.size());
        for (Object obj : objects) {
            result.add(clazz.cast(obj));
        }
        return result;
    }

    public long getZsetRank(String key, Object value) {
        return Objects.requireNonNullElse(redisTemplate.opsForZSet().rank(key, value), -1L);
    }
}
