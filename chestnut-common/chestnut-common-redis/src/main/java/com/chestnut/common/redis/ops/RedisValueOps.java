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

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.CacheLoader;
import com.chestnut.common.redis.CacheObject;
import com.chestnut.common.redis.RedisNullValue;
import com.chestnut.common.redis.config.RedisCacheProperties;
import com.chestnut.common.utils.ConvertUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Redis String / 通用 value 操作组件。
 *
 * <p>同时承载三害防护逻辑：
 * <ul>
 *   <li>防穿透：{@link RedisNullValue} 占位，{@link #loadIfAbsent} 自动处理 null 回填</li>
 *   <li>防雪崩：{@link #loadIfAbsent} 带 TTL 的路径通过 {@link AbstractRedisOps#jitter(long)} 按需施加抖动</li>
 *   <li>防击穿：{@link #loadIfAbsentWithLock} 基于 Redisson 分布式锁</li>
 * </ul>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisValueOps extends AbstractRedisOps {

    private static final String LOCK_PREFIX = "cache_lock:";
    private static final int LOCK_WAIT_SECONDS = 10;

    private final boolean allowNullValue;
    private final RedissonClient redissonClient;

    public RedisValueOps(RedisTemplate<String, Object> redisTemplate,
                         RedisCacheConfiguration config,
                         RedisCacheProperties cacheProperties,
                         RedissonClient redissonClient) {
        super(redisTemplate, cacheProperties);
        this.allowNullValue = config.getAllowCacheNullValues();
        this.redissonClient = redissonClient;
    }

    // -------------------------------------------------------------------------
    // 写入
    // -------------------------------------------------------------------------

    public void setCacheObject(String key, Object value) {
        if (value == null) {
            if (!allowNullValue) {
                throw CommonErrorCode.CACHE_NULL_NOT_ALLOWED.exception(key);
            }
            redisTemplate.opsForValue().set(key, RedisNullValue.INSTANCE,
                    cacheProperties.getNullValueExpireSeconds(), TimeUnit.SECONDS);
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> void setCacheObject(String key, T value, long timeout, TimeUnit timeUnit) {
        if (value == null) {
            if (!allowNullValue) {
                throw CommonErrorCode.CACHE_NULL_NOT_ALLOWED.exception(key);
            }
            redisTemplate.opsForValue().set(key, RedisNullValue.INSTANCE,
                    cacheProperties.getNullValueExpireSeconds(), TimeUnit.SECONDS);
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeUnit.toSeconds(timeout), TimeUnit.SECONDS);
    }

    /**
     * 写入缓存并对 TTL 施加随机抖动（防雪崩），适合缓存场景。
     * 仅当 {@code chestnut.redis.ttlJitter=true} 时实际施加抖动，否则行为等同于
     * {@link #setCacheObject(String, Object, long, TimeUnit)}。
     * 业务精确 TTL（验证码、令牌等）请使用 {@link #setCacheObject(String, Object, long, TimeUnit)}。
     */
    public void setCacheObjectWithJitter(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (value == null) {
            if (!allowNullValue) {
                throw CommonErrorCode.CACHE_NULL_NOT_ALLOWED.exception(key);
            }
            redisTemplate.opsForValue().set(key, RedisNullValue.INSTANCE,
                    jitter(cacheProperties.getNullValueExpireSeconds()), TimeUnit.SECONDS);
            return;
        }
        redisTemplate.opsForValue().set(key, value, jitter(timeUnit.toSeconds(timeout)), TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // 读取
    // -------------------------------------------------------------------------

    public <T> T getCacheObject(String key, Class<T> clazz) {
        Object raw = redisTemplate.opsForValue().get(key);
        return unwrap(raw, clazz);
    }

    public <T> List<T> getCacheObjects(Collection<String> keys, Class<T> clazz) {
        List<Object> rawList = redisTemplate.opsForValue().multiGet(keys);
        if (rawList == null) {
            return List.of();
        }
        List<T> result = new ArrayList<>(rawList.size());
        for (Object raw : rawList) {
            result.add(unwrap(raw, clazz));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // loadIfAbsent 缓存填充模板（防穿透 + 可配置 jitter）
    // -------------------------------------------------------------------------

    /**
     * 读取缓存，miss 时调用 loader 回填（无 TTL，永久缓存）。
     * loader 返回 null 时根据 {@code allowNullValue} 决定是否写入占位。
     */
    public <T> T loadIfAbsent(String key, Class<T> clazz, CacheLoader<T> loader) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw != null) {
            return unwrap(raw, clazz);
        }
        T loaded = loader.load();
        writeBack(key, loaded, 0, null);
        return loaded;
    }

    /**
     * 读取缓存，miss 时调用 loader 回填；TTL 是否施加抖动由 {@code chestnut.redis.ttlJitter} 开关控制。
     * loader 返回 null 时根据 {@code allowNullValue} 决定是否写入占位（短 TTL）。
     */
    public <T> T loadIfAbsent(String key, Class<T> clazz, CacheLoader<T> loader, long timeout, TimeUnit timeUnit) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw != null) {
            return unwrap(raw, clazz);
        }
        T loaded = loader.load();
        writeBack(key, loaded, timeout, timeUnit);
        return loaded;
    }

    /**
     * 读取缓存，miss 时用 Redisson 分布式锁保护回源（防击穿 + 防穿透 + 防雪崩）。
     * 锁等待超时（{@value LOCK_WAIT_SECONDS}s）后退化为无锁直接回源，保证可用性。
     */
    public <T> T loadIfAbsentWithLock(String key, Class<T> clazz, CacheLoader<T> loader,
                                      long timeout, TimeUnit timeUnit) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw != null) {
            return unwrap(raw, clazz);
        }
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                return loader.load();
            }
            // double-check
            raw = redisTemplate.opsForValue().get(key);
            if (raw != null) {
                return unwrap(raw, clazz);
            }
            T loaded = loader.load();
            writeBack(key, loaded, timeout, timeUnit);
            return loaded;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return loader.load();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // -------------------------------------------------------------------------
    // 自增
    // -------------------------------------------------------------------------

    public void incrCacheValue(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public void incrCacheValue(String key, long delta) {
        redisTemplate.opsForValue().increment(key, delta);
    }

    public Long getLongCacheValue(String key) {
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        return ConvertUtils.toLong(valueOps.get(key), 0L);
    }

    public long atomicLongIncr(String key) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return redisAtomicLong.incrementAndGet();
    }

    // -------------------------------------------------------------------------
    // Bit 操作（归属 value 层）
    // -------------------------------------------------------------------------

    public void setBit(String key, long offset, boolean value) {
        redisTemplate.opsForValue().setBit(key, offset, value);
    }

    public boolean getBit(String key, long offset) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, offset));
    }

    // -------------------------------------------------------------------------
    // 内部工具
    // -------------------------------------------------------------------------

    /**
     * 将 Redis 原始值解包，{@link RedisNullValue} 占位视同 null 返回。
     */
    @SuppressWarnings("unchecked")
    static <T> T unwrap(Object raw, Class<T> clazz) {
        if (raw == null || raw instanceof RedisNullValue) {
            return null;
        }
        if (clazz == null) {
            return (T) raw;
        }
        return clazz.cast(raw);
    }

    /**
     * 统一回填入口，处理 null 占位；TTL 是否加 jitter 由 {@code chestnut.redis.ttlJitter} 开关决定。
     */
    private void writeBack(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (value == null) {
            if (allowNullValue) {
                redisTemplate.opsForValue().set(key, RedisNullValue.INSTANCE,
                        jitter(cacheProperties.getNullValueExpireSeconds()), TimeUnit.SECONDS);
            }
            return;
        }
        if (timeout > 0 && timeUnit != null) {
            redisTemplate.opsForValue().set(key, value, jitter(timeUnit.toSeconds(timeout)), TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 兼容旧 {@code getCacheObjectWithExpiresIn} 的 CacheObject 封装回填。
     */
    public <T> T loadWithCacheObject(String key, Class<T> clazz,
                                     java.util.function.Supplier<CacheObject<T>> supplier) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw != null) {
            return unwrap(raw, clazz);
        }
        CacheObject<T> co = supplier.get();
        if (co == null) {
            return null;
        }
        writeBack(key, co.getData(), co.getExpiresIn() != null ? co.getExpiresIn() : 0L, co.getTimeUnit());
        return co.getData();
    }
}
