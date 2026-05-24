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

import com.chestnut.common.redis.ops.*;
import com.chestnut.common.utils.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 缓存工具类门面。
 *
 * <p>内部按数据结构拆分为 8 个子组件（{@code RedisXxxOps}），本类仅做转发，保持所有原有 API 向后兼容。
 *
 * <p>新增 API（防三害）：
 * <ul>
 *   <li>{@link #loadIfAbsent} — 缓存填充模板，支持防穿透（null 占位）+ 防雪崩（TTL jitter）</li>
 *   <li>{@link #loadIfAbsentWithLock} — 在 loadIfAbsent 基础上加 Redisson 分布式锁防击穿</li>
 *   <li>{@link #setCacheObjectWithJitter} — 写缓存时对 TTL 施加随机抖动（防雪崩）</li>
 * </ul>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisCache {

	@Getter
    private final RedisTemplate<String, Object> redisTemplate;
	@Getter
    private final RedisValueOps valueOps;
	@Getter
    private final RedisHashOps hashOps;
	@Getter
    private final RedisListOps listOps;
	@Getter
    private final RedisSetOps setOps;
	@Getter
    private final RedisZSetOps zsetOps;
	@Getter
    private final RedisHyperLogLogOps hllOps;
	@Getter
    private final RedisKeyOps keyOps;

    public RedisCache(RedisTemplate<String, Object> redisTemplate,
                      RedisValueOps valueOps,
                      RedisHashOps hashOps,
                      RedisListOps listOps,
                      RedisSetOps setOps,
                      RedisZSetOps zsetOps,
                      RedisHyperLogLogOps hllOps,
                      RedisKeyOps keyOps) {
        this.redisTemplate = redisTemplate;
        this.valueOps = valueOps;
        this.hashOps = hashOps;
        this.listOps = listOps;
        this.setOps = setOps;
        this.zsetOps = zsetOps;
        this.hllOps = hllOps;
        this.keyOps = keyOps;
    }

    // =========================================================================
    // String / Value 操作
    // =========================================================================

    /**
     * 缓存基本对象（Integer、String、实体类等）。
     * value 为 null 时：若配置允许则写入占位（短 TTL），否则抛出异常。
     */
    public void setCacheObject(final String key, final Object value) {
        valueOps.setCacheObject(key, value);
    }

    /**
     * 缓存基本对象并指定精确过期时间（业务 TTL，不施加 jitter）。
     */
    public <T> void setCacheObject(final String key, final T value, final long timeout, final TimeUnit timeUnit) {
        valueOps.setCacheObject(key, value, timeout, timeUnit);
    }

    /**
     * 缓存基本对象并对 TTL 施加随机抖动（防雪崩）。
     * 仅适合缓存场景；验证码、令牌等业务精确 TTL 请使用 {@link #setCacheObject(String, Object, long, TimeUnit)}。
     */
    public void setCacheObjectWithJitter(final String key, final Object value,
                                         final long timeout, final TimeUnit timeUnit) {
        valueOps.setCacheObjectWithJitter(key, value, timeout, timeUnit);
    }

    /**
     * 读取缓存，miss 时通过 loader 回填（无 TTL，适合需要持久缓存的场景）。
     * loader 返回 null 视为"数据源中确实不存在"，会自动写入占位（防穿透）。
     *
     * <p><b>注意</b>：数据源异常时应抛出异常而非返回 null，否则故障期间会被错误缓存。
     */
    public <T> T loadIfAbsent(final String key, final Class<T> clazz, final CacheLoader<T> loader) {
        return valueOps.loadIfAbsent(key, clazz, loader);
    }

    /**
     * 读取缓存，miss 时通过 loader 回填（TTL 自动施加 jitter，防雪崩）。
     * loader 返回 null 视为"数据源中确实不存在"，会自动写入占位（防穿透）。
     */
    public <T> T loadIfAbsent(final String key, final Class<T> clazz, final CacheLoader<T> loader,
                              final long timeout, final TimeUnit timeUnit) {
        return valueOps.loadIfAbsent(key, clazz, loader, timeout, timeUnit);
    }

    /**
     * 读取缓存，miss 时用 Redisson 分布式锁保护回源（防击穿 + 防穿透 + 防雪崩）。
     * 锁等待超时（10s）后退化为无锁直接回源，保证可用性。
     */
    public <T> T loadIfAbsentWithLock(final String key, final Class<T> clazz, final CacheLoader<T> loader,
                                      final long timeout, final TimeUnit timeUnit) {
        return valueOps.loadIfAbsentWithLock(key, clazz, loader, timeout, timeUnit);
    }

    /**
     * 获得缓存对象。
     */
    public <T> T getCacheObject(final String key, final Class<T> clazz) {
        return valueOps.getCacheObject(key, clazz);
    }

    /**
     * 批量获取多个缓存对象。
     */
    public <T> List<T> getCacheObjects(final Collection<String> keys, final Class<T> clazz) {
        if (StringUtils.isEmpty(keys)) {
            return List.of();
        }
        return valueOps.getCacheObjects(keys, clazz);
    }

    /**
     * 获得缓存对象，miss 时调用 supplier 回填（无 TTL）。
     * supplier 返回 null 时写入占位防穿透。
     */
    public <T> T getCacheObject(final String key, final Class<T> clazz, final Supplier<T> supplier) {
        return valueOps.loadIfAbsent(key, clazz, supplier != null ? supplier::get : null);
    }

    /**
     * 获得缓存对象，miss 时调用 supplier 回填，TTL 由 supplier 返回的 CacheObject 指定。
     */
    public <T> T getCacheObjectWithExpiresIn(final String key, final Class<T> clazz,
                                             final Supplier<CacheObject<T>> supplier) {
        return valueOps.loadWithCacheObject(key, clazz, supplier);
    }

    public void incrCacheValue(final String key) {
        valueOps.incrCacheValue(key);
    }

    public void incrCacheValue(final String key, final long delta) {
        valueOps.incrCacheValue(key, delta);
    }

    public Long getLongCacheValue(final String key) {
        return valueOps.getLongCacheValue(key);
    }

    // =========================================================================
    // Key 操作
    // =========================================================================

    public boolean expire(final String key, final long timeout) {
        return keyOps.expire(key, timeout);
    }

    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return keyOps.expire(key, timeout, unit);
    }

    public long getExpire(final String key) {
        return keyOps.getExpire(key);
    }

    public long getExpire(final String key, final TimeUnit unit) {
        return keyOps.getExpire(key, unit);
    }

    public boolean hasKey(final String key) {
        return keyOps.hasKey(key);
    }

    public boolean deleteObject(final String key) {
        return keyOps.deleteObject(key);
    }

    public boolean deleteObjects(final Collection<String> cacheKeys) {
        return keyOps.deleteObjects(cacheKeys);
    }

    public long deleteByPrefix(final String prefix) {
        return keyOps.deleteByPrefix(prefix);
    }

    public Collection<String> keys(final String pattern) {
        return keyOps.keys(pattern);
    }

    public Set<String> scanKeys(final String pattern, final int count) {
        return keyOps.scanKeys(pattern, count);
    }

    public Set<String> scanKeys(final DataType dataType, final String pattern, final int count) {
        return keyOps.scanKeys(dataType, pattern, count);
    }

    // =========================================================================
    // Hash 操作
    // =========================================================================

    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        hashOps.setCacheMap(key, dataMap);
    }

    /**
     * 写入 Hash 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap,
                                final long ttl, final TimeUnit timeUnit) {
        hashOps.setCacheMap(key, dataMap, ttl, timeUnit);
    }

    public <T> Map<String, T> getCacheMap(final String key, final Class<T> clazz) {
        return hashOps.getCacheMap(key, clazz);
    }

    public <T> Map<String, T> getCacheMap(final String key, final Class<T> clazz,
                                          final Supplier<Map<String, T>> supplier) {
        return hashOps.getCacheMap(key, clazz, supplier);
    }

    /**
     * 获取 Hash 缓存，miss 时调用 supplier 回填并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> Map<String, T> getCacheMap(final String key, final Class<T> clazz,
                                          final long ttl, final TimeUnit timeUnit,
                                          final Supplier<Map<String, T>> supplier) {
        return hashOps.getCacheMap(key, clazz, ttl, timeUnit, supplier);
    }

    public void incrMapValue(final String cacheKey, final String hKey, final long delta) {
        hashOps.incrMapValue(cacheKey, hKey, delta);
    }

    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        hashOps.setCacheMapValue(key, hKey, value);
    }

    public <T> void setCacheMapValues(final String key, final Map<String, T> map) {
        hashOps.setCacheMapValues(key, map);
    }

    public <T> T getCacheMapValue(final String key, final String hKey) {
        return hashOps.getCacheMapValue(key, hKey);
    }

    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<String> hKeys) {
        return hashOps.getMultiCacheMapValue(key, hKeys);
    }

    public boolean deleteCacheMapValue(final String key, final Object... hKeys) {
        return hashOps.deleteCacheMapValue(key, hKeys);
    }

    public boolean hasMapKey(final String cacheKey, final String hashKey) {
        return hashOps.hasMapKey(cacheKey, hashKey);
    }

    // =========================================================================
    // List 操作
    // =========================================================================

    public <T> List<T> getCacheList(final String key, final Class<T> clazz) {
        return listOps.getCacheList(key, clazz);
    }

    @NotNull
    public <T> List<T> getCacheList(final String key, final Class<T> clazz, final Supplier<List<T>> supplier) {
        return listOps.getCacheList(key, clazz, supplier);
    }

    public <T> void listRightPush(final String key, final Collection<T> list) {
        listOps.listRightPush(key, list);
    }

    /**
     * 批量右推入 List 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public <T> void listRightPush(final String key, final Collection<T> list,
                                  final long ttl, final TimeUnit timeUnit) {
        listOps.listRightPush(key, list, ttl, timeUnit);
    }

    // =========================================================================
    // Set 操作
    // =========================================================================

    public <T> void setCacheSet(final String key, final Set<T> dataSet) {
        setOps.setCacheSet(key, dataSet);
    }

    public <T> void setCacheSet(final String key, final Set<T> dataSet, final long ttl, final TimeUnit timeUnit) {
        setOps.setCacheSet(key, dataSet, ttl, timeUnit);
    }

    public <T> Set<T> getCacheSet(final String key, final Class<T> clazz) {
        return setOps.getCacheSet(key, clazz);
    }

    public boolean isSetContainsAny(final String key, final Object... value) {
        return setOps.isSetContainsAny(key, value);
    }

    public boolean isSetContains(final String key, final Object value) {
        return setOps.isSetContains(key, value);
    }

    public Long getCacheSetSize(final String key) {
        return setOps.getCacheSetSize(key);
    }

    public void addSetValue(final String key, final Object... values) {
        setOps.addSetValue(key, values);
    }

    public void removeSetValue(final String key, final Object... values) {
        setOps.removeSetValue(key, values);
    }

    public <T> T randomSetValue(final String key, final Class<T> clazz) {
        return setOps.randomSetValue(key, clazz);
    }

    public <T> List<T> randomSetValues(final String key, final long count, final Class<T> clazz) {
        return setOps.randomSetValues(key, count, clazz);
    }

    // =========================================================================
    // ZSet 操作
    // =========================================================================

    public boolean addZset(final String key, final Object value, final double score) {
        return zsetOps.addZset(key, value, score);
    }

    /**
     * 添加元素到 ZSet 并设置过期时间（TTL 自动施加 jitter，防雪崩）。
     */
    public boolean addZset(final String key, final Object value, final double score,
                           final long ttl, final TimeUnit timeUnit) {
        return zsetOps.addZset(key, value, score, ttl, timeUnit);
    }

    public long zsetIncr(final String key, final String value, final double delta) {
        return zsetOps.zsetIncr(key, value, delta);
    }

    public Double getZsetScore(final String key, final String value) {
        return zsetOps.getZsetScore(key, value);
    }

    public boolean isValueInZset(final String key, final Object value) {
        return zsetOps.isValueInZset(key, value);
    }

    public void removeZsetValue(final String key, final Object... values) {
        zsetOps.removeZsetValue(key, values);
    }

    public void removeZsetValueByScore(final String key, final double min, final double max) {
        zsetOps.removeZsetValueByScore(key, min, max);
    }

    public long getZsetSize(final String key) {
        return zsetOps.getZsetSize(key);
    }

    public <T> Set<T> getZset(final String key, final int start, final int end, final Class<T> clazz) {
        return zsetOps.getZset(key, start, end, clazz);
    }

    public long getZsetRank(final String key, final Object value) {
        return zsetOps.getZsetRank(key, value);
    }

    // =========================================================================
    // Bit 操作
    // =========================================================================

    public void setBit(final String cacheKey, final long offset, final boolean value) {
        valueOps.setBit(cacheKey, offset, value);
    }

    public boolean getBit(final String cacheKey, final long offset) {
        return valueOps.getBit(cacheKey, offset);
    }

    // =========================================================================
    // HyperLogLog 操作
    // =========================================================================

    public void addHyperLogLog(final String cacheKey, final Object... values) {
        hllOps.addHyperLogLog(cacheKey, values);
    }

    public Long getHyperLogLogSize(final String cacheKey) {
        return hllOps.getHyperLogLogSize(cacheKey);
    }

    public void removeHyperLogLog(final String cacheKey) {
        hllOps.removeHyperLogLog(cacheKey);
    }

    // =========================================================================
    // 自增
    // =========================================================================

    /**
     * 获得自增 ID。
     */
    public long atomicLongIncr(final String key) {
        return valueOps.atomicLongIncr(key);
    }
}
