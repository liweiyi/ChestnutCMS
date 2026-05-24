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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis List 操作组件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisListOps extends AbstractRedisOps {

    public RedisListOps(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties cacheProperties) {
        super(redisTemplate, cacheProperties);
    }

    public <T> List<T> getCacheList(String key, Class<T> clazz) {
        List<Object> objectList = redisTemplate.opsForList().range(key, 0, -1);
        if (objectList == null) {
            return List.of();
        }
        return castList(objectList, clazz);
    }

    public <T> List<T> getCacheList(String key, Class<T> clazz, Supplier<List<T>> supplier) {
        if (!redisTemplate.hasKey(key)) {
            if (Objects.nonNull(supplier)) {
                List<T> list = supplier.get();
                if (Objects.nonNull(list)) {
                    listRightPush(key, list);
                    return list;
                }
            }
            return List.of();
        }
        List<Object> cacheValue = redisTemplate.opsForList().range(key, 0, -1);
        if (cacheValue == null) {
            return List.of();
        }
        return castList(cacheValue, clazz);
    }

    /**
     * 批量右推入 List（一次网络往返）。
     */
    public <T> void listRightPush(String key, Collection<T> list) {
        if (StringUtils.isEmpty(list)) {
            return;
        }
        redisTemplate.opsForList().rightPushAll(key, list.toArray());
    }

    /**
     * 批量右推入 List 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> void listRightPush(String key, Collection<T> list, long ttl, TimeUnit timeUnit) {
        if (StringUtils.isEmpty(list)) {
            return;
        }
        redisTemplate.opsForList().rightPushAll(key, list.toArray());
        if (ttl > 0 && timeUnit != null) {
            redisTemplate.expire(key, jitter(timeUnit.toSeconds(ttl)), TimeUnit.SECONDS);
        }
    }

    private <T> List<T> castList(List<Object> rawList, Class<T> clazz) {
        List<T> result = new ArrayList<>(rawList.size());
        for (Object obj : rawList) {
            result.add(clazz.cast(obj));
        }
        return result;
    }
}
