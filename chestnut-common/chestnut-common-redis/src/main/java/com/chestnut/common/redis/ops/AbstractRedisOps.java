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
import com.chestnut.common.redis.util.TtlJitter;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis Ops 子组件抽象基类，包内可见。
 *
 * <p>提供共享的 {@link #jitter(long)} 工具方法，由 {@code RedisCacheProperties.ttlJitter}
 * 开关统一控制是否对缓存 TTL 施加随机抖动（防雪崩）。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
abstract class AbstractRedisOps {

    protected final RedisTemplate<String, Object> redisTemplate;
    protected final RedisCacheProperties cacheProperties;

    AbstractRedisOps(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties cacheProperties) {
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
    }

    /**
     * 根据 {@code chestnut.redis.ttlJitter} 开关决定是否对缓存 TTL 施加随机抖动。
     * ttl ≤ 0 时直接返回原值。
     */
    protected long jitter(long ttl) {
        return cacheProperties.isTtlJitter() ? TtlJitter.apply(ttl) : ttl;
    }
}
