/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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

import com.chestnut.common.utils.StringUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * spring redis 工具类
 **/
@Component
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class RedisCache {

	public final RedisTemplate redisTemplate;

	/**
	 * 是否允许缓存null
	 */
	private final boolean allowNullValue;

	/**
	 * null值缓存默认过期时间30秒
	 */
	private final long nullValueExpire = 30;

	public RedisCache(RedisTemplate<String, Object> redisTemplate, RedisCacheConfiguration config) {
		this.redisTemplate = redisTemplate;
		this.allowNullValue = config.getAllowCacheNullValues();
	}

	/**
	 * 缓存基本的对象，Integer、String、实体类等
	 *
	 * @param key   缓存的键值
	 * @param value 缓存的值
	 */
	public <T> void setCacheObject(final String key, final T value) {
		if (Objects.isNull(value) && !this.allowNullValue) {
			return;
		}
		if (Objects.isNull(value)) {
			redisTemplate.opsForValue().set(key, value, this.nullValueExpire, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForValue().set(key, value);
		}
	}

	/**
	 * 缓存基本的对象，Integer、String、实体类等
	 *
	 * @param key      缓存的键值
	 * @param value    缓存的值
	 * @param timeout  时间
	 * @param timeUnit 时间颗粒度
	 */
	public <T> void setCacheObject(final String key, final T value, final long timeout, final TimeUnit timeUnit) {
		if (Objects.isNull(value) && !this.allowNullValue) {
			return;
		}
		if (Objects.isNull(value)) {
			redisTemplate.opsForValue().set(key, value, this.nullValueExpire, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
		}
	}

	/**
	 * 设置有效时间
	 *
	 * @param key     Redis键
	 * @param timeout 超时时间
	 * @return true=设置成功；false=设置失败
	 */
	public boolean expire(final String key, final long timeout) {
		return expire(key, timeout, TimeUnit.SECONDS);
	}

	/**
	 * 设置有效时间
	 *
	 * @param key     Redis键
	 * @param timeout 超时时间
	 * @param unit    时间单位
	 * @return true=设置成功；false=设置失败
	 */
	public boolean expire(final String key, final long timeout, final TimeUnit unit) {
		return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
	}

	/**
	 * 获取有效时间
	 *
	 * @param key Redis键
	 * @return 有效时间
	 */
	public long getExpire(final String key) {
		return Objects.requireNonNullElse(redisTemplate.getExpire(key), 0L);
	}

	public long getExpire(final String key, final TimeUnit unit) {
		return Objects.requireNonNullElse(redisTemplate.getExpire(key, unit), 0L);
	}

	/**
	 * 判断 key是否存在
	 *
	 * @param key 键
	 * @return true 存在 false不存在
	 */
	public boolean hasKey(String key) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	public boolean hasMapKey(String cacheKey, String hashKey) {
		return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(cacheKey, hashKey));
	}

	/**
	 * 获得缓存的基本对象。
	 *
	 * @param key 缓存键值
	 * @return 缓存键值对应的数据
	 */
	public <T> T getCacheObject(final String key) {
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}

	/**
	 * 获得缓存的基本对象，如果不存在调用supplier获取数据。
	 *
	 * @param cacheKey 缓存Key
	 * @param supplier 未获取到缓存结果提供默认数据
	 */
	public <T> T getCacheObject(String cacheKey, Supplier<T> supplier) {
		T cacheObject = this.getCacheObject(cacheKey);
		if (Objects.isNull(cacheObject) && Objects.nonNull(supplier)) {
			cacheObject = supplier.get();
			if (Objects.nonNull(cacheObject)) {
				this.setCacheObject(cacheKey, cacheObject);
			}
		}
		return cacheObject;
	}

	public <T> T getCacheObjectWithExpiresIn(String cacheKey, Supplier<CacheObject<T>> supplier) {
		T cacheObject = this.getCacheObject(cacheKey);
		if (Objects.isNull(cacheObject) && Objects.nonNull(supplier)) {
			CacheObject<T> co = supplier.get();
			if (Objects.nonNull(co.getData())) {
				this.setCacheObject(cacheKey, co.getData(), co.getExpiresIn(), co.getTimeUnit());
			}
			cacheObject = co.getData();
		}
		return cacheObject;
	}

	/**
	 * 删除单个对象
	 *
	 * @param key 缓存Key
	 */
	public boolean deleteObject(final String key) {
		return Boolean.TRUE.equals(redisTemplate.delete(key));
	}

	/**
	 * 删除集合对象
	 *
	 * @param collection 多个对象
	 */
	public boolean deleteObject(final Collection collection) {
		Long delete = redisTemplate.delete(collection);
		return Objects.nonNull(delete) && delete > 0;
	}

	/**
	 * 缓存List数据
	 *
	 * @param key      缓存的键值
	 * @param dataList 待缓存的List数据
	 * @return 缓存的对象
	 */
	public <T> void setCacheList(final String key, @NotNull final List<T> dataList) {
		if (Objects.isNull(dataList)) {
			return;
		}
		redisTemplate.opsForList().rightPushAll(key, dataList);
	}

	/**
	 * 获得缓存的list对象
	 *
	 * @param key 缓存的键值
	 * @return 缓存键值对应的数据
	 */
	public <T> List<T> getCacheList(final String key) {
		return redisTemplate.opsForList().range(key, 0, -1);
	}

	/**
	 * 缓存Set
	 *
	 * @param key     缓存键值
	 * @param dataSet 缓存的数据，为null时替换成EmptySet
	 */
	public <T> void setCacheSet(final String key, Set<T> dataSet) {
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		if (Objects.isNull(dataSet)) {
			dataSet = Set.of();
		}
		for (T t : dataSet) {
			setOperation.add(t);
		}
	}

	/**
	 * 获得缓存的set
	 *
	 * @param key 缓存Key
	 * @return 缓存数据
	 */
	public <T> Set<T> getCacheSet(final String key) {
		return redisTemplate.opsForSet().members(key);
	}

	/**
	 * Add element to set cache
	 *
	 * @param key Cache key
	 * @param values Set Element
	 */
	public void addSetValue(final String key, final Object... values) {
		if (StringUtils.isEmpty(values)) {
			return;
		}
		this.redisTemplate.opsForSet().add(key, values);
	}

	/**
	 * Remove element from set cache
	 *
	 * @param key Cache key
	 * @param values Set Elements
	 */
	public void removeSetValue(final String key, final Object... values) {
		if (StringUtils.isEmpty(values)) {
			return;
		}
		this.redisTemplate.opsForSet().remove(key, values);
	}

	/**
	 * Set map cache
	 *
	 * @param key Cache key
	 * @param dataMap Cache Value, replace to EmptyMap if null.
	 */
	public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
		redisTemplate.opsForHash().putAll(key, Objects.requireNonNullElse(dataMap, Map.of()));
	}

	/**
	 * Get map cache
	 *
	 * @param key Cache key
	 * @return Cached map
	 */
	public <T> Map<String, T> getCacheMap(final String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	public <T> Map<String, T> getCacheMap(final String key, Supplier<Map<String, T>> supplier) {
		if (!Objects.requireNonNullElse(redisTemplate.hasKey(key), false)) {
			this.setCacheMap(key, Objects.requireNonNullElse(supplier.get(), Map.of()));
		}
		return redisTemplate.opsForHash().entries(key);
	}

	public void incrMapValue(String cacheKey, String hKey, long delta) {
		redisTemplate.opsForHash().increment(cacheKey, hKey, delta);
	}

	/**
	 * 往Hash中存入数据
	 *
	 * @param key   Redis键
	 * @param hKey  Hash键
	 * @param value 值
	 */
	public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
		redisTemplate.opsForHash().put(key, hKey, value);
	}

	/**
	 * 获取Hash中的数据
	 *
	 * @param key  Redis键
	 * @param hKey Hash键
	 * @return Hash中的对象
	 */
	public <T> T getCacheMapValue(final String key, final String hKey) {
		HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
		return opsForHash.get(key, hKey);
	}

	/**
	 * 获取多个Hash中的数据
	 *
	 * @param key   Redis键
	 * @param hKeys Hash键集合
	 * @return Hash对象集合
	 */
	public <T> List<T> getMultiCacheMapValue(final String key, final Collection<String> hKeys) {
		return redisTemplate.opsForHash().multiGet(key, hKeys);
	}

	/**
	 * 删除Hash中的某条数据
	 *
	 * @param key  Redis键
	 * @param hKeys Hash键
	 * @return 是否成功
	 */
	public boolean deleteCacheMapValue(final String key, final Object... hKeys) {
		if (StringUtils.isEmpty(hKeys)) {
			return false;
		}
		return redisTemplate.opsForHash().delete(key, hKeys) > 0;
	}

	/**
	 * 获得缓存的基本对象列表
	 *
	 * @param pattern 字符串前缀
	 * @return 对象列表
	 */
	public Collection<String> keys(final String pattern) {
		return redisTemplate.keys(pattern);
	}

	/**
	 * 获得匹配的n条缓存键名列表
	 *
	 * @param pattern 匹配正则
	 * @return cache key set
	 */
	public Set<String> scanKeys(final String pattern, final int count) {
		return scanKeys(DataType.STRING, pattern, count);
	}

	/**
	 * 获得匹配的n条缓存键名列表
	 *
	 * @param dataType 缓存数据类型
	 * @param pattern 匹配正则
	 * @param count 数量
	 * @return cache key set
	 */
	public Set<String> scanKeys(DataType dataType, final String pattern, int count) {
		if (Objects.isNull(dataType) || DataType.NONE.equals(dataType)) {
			return Set.of();
		}
		return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			Set<String> keysTmp = new HashSet<>();
			KeyScanOptions options = (KeyScanOptions) KeyScanOptions.scanOptions(dataType).match(pattern).count(count)
					.build();
			Cursor<byte[]> cursor = connection.scan(options);
			while (cursor.hasNext()) {
				keysTmp.add(new String(cursor.next(), StandardCharsets.UTF_8));
			}
			return keysTmp;
		});
	}

	/**
	 * 获得自增ID
	 *
	 * @param key Cache key
	 * @return longValue
	 */
	public long atomicLongIncr(String key) {
		RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, this.redisTemplate);
		return redisAtomicLong.incrementAndGet();
	}

	/**
	 * 添加zset
	 *
	 * @param key Cache key
	 * @param value Cache value
	 * @param score Cache score
	 * @return boolean
	 */
	public Boolean addZset(String key, Object value, double score) {
		return redisTemplate.opsForZSet().add(key, value, score);
	}

	/**
	 * ZSet.score + delta
	 *
	 * @param key Cache key
	 * @param value Cache value
	 * @param delta score delta
	 * @return score
	 */
	public long zsetIncr(String key, String value, double delta) {
		return Objects.requireNonNull(this.redisTemplate.opsForZSet().incrementScore(key, value, delta)).longValue();
	}

	/**
	 * 获取zset.score，不存在返回-1
	 *
	 * @param key Cache key
	 * @param value Cache value
	 * @return score
	 */
	public Double getZsetScore(String key, String value) {
		Double score = this.redisTemplate.opsForZSet().score(key, value);
		return Objects.requireNonNullElse(score, -1d);
	}

	public boolean isValueInZset(String key, Object value) {
		return Objects.nonNull(this.redisTemplate.opsForZSet().score(key, value));
	}

	/**
	 * 删除zset
	 *
	 * @param key Cache key
	 * @param values Cache values
	 */
	public void removeZsetValue(String key, Object... values) {
		if (StringUtils.isEmpty(values)) {
			return;
		}
		this.redisTemplate.opsForZSet().remove(key, values);
	}

	/**
	 * zset.size
	 *
	 * @param key Cache key
	 * @return zset.size
	 */
	public long getZsetSize(String key) {
		return Objects.requireNonNullElse(this.redisTemplate.opsForZSet().size(key), 0L);
	}

	/**
	 * 获取指定范围zset数据，end=-1表示取所有
	 *
	 * @param key Cache key
	 * @param start start index
	 * @param end end index
	 * @return cache set
	 */
	public <T> Set<T> getZset(String key, int start, int end) {
		return this.redisTemplate.opsForZSet().range(key, start, end);
	}

	/**
	 * 获取指定值排名，不存在返回-1，0表示第一位
	 *
	 * @param key Cache key
	 * @param value Cache value
	 * @return rank
	 */
	public long getZsetRank(String key, Object value) {
		return Objects.requireNonNullElse(redisTemplate.opsForZSet().rank(key, value), -1L);
	}

	/**
     * 设置cacheKey字段第offset位bit数值
     *
     * @param cacheKey Cache key
     * @param offset 位置
     * @param value  值
     */
    public void setBit(String cacheKey, long offset, boolean value) {
		this.redisTemplate.opsForValue().setBit(cacheKey, offset, value);
    }
    
    /**
     * 判断该key字段offset位否为1
     *
     * @param cacheKey Cache key
     * @param offset 位置
     * @return boolean
     */
    public boolean getBit(String cacheKey, long offset) {
        return Boolean.TRUE.equals(this.redisTemplate.opsForValue().getBit(cacheKey, offset));
    }

	public void incrLongCounter(String cacheKey) {
		this.redisTemplate.opsForValue().increment(cacheKey);
	}

	public void incrLongCounter(String cacheKey, long delta) {
		this.redisTemplate.opsForValue().increment(cacheKey, delta);
	}

	public Long getLongCounterValue(String cacheKey) {
		ValueOperations<String, Integer> valueOperations = this.redisTemplate.opsForValue();
		Integer v = valueOperations.get(cacheKey);
		return Objects.isNull(v) ? null : v.longValue();
	}

	public Long getLongCounterValue(String cacheKey, Supplier<Long> supplier) {
		ValueOperations<String, Integer> valueOperations = this.redisTemplate.opsForValue();
		Integer obj = valueOperations.get(cacheKey);
		if (Objects.isNull(obj)) {
			Long initV = supplier.get();
			incrLongCounter(cacheKey, initV);
			return initV;
		}
		return obj.longValue();
	}
}
