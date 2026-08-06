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
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyScanOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis Key 级别操作组件（TTL / 删除 / SCAN）
 *
 * <p>注意：{@link #expire} 是通用 Key 级别操作，可用于业务精确 TTL（令牌、限流窗口等），
 * 不自动施加 jitter。如需缓存语义的带 jitter 过期，请通过各数据结构的写入方法传入 TTL 参数。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class RedisKeyOps extends AbstractRedisOps {

    public RedisKeyOps(RedisTemplate<String, Object> redisTemplate, RedisCacheProperties cacheProperties) {
        super(redisTemplate, cacheProperties);
    }

    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public long getExpire(String key) {
        return Objects.requireNonNullElse(redisTemplate.getExpire(key), 0L);
    }

    public long getExpire(String key, TimeUnit unit) {
        return Objects.requireNonNullElse(redisTemplate.getExpire(key, unit), 0L);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean deleteObject(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean deleteObjects(Collection<String> cacheKeys) {
        Long deleted = redisTemplate.delete(cacheKeys);
        return Objects.requireNonNullElse(deleted, 0L) > 0;
    }

    /**
     * 使用 SCAN 分批删除指定前缀的所有 key，避免 KEYS 阻塞。
     *
     * @param prefix key 前缀
     * @return 实际删除的 key 数量
     */
    public long deleteByPrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return 0;
        }
        String pattern = prefix + "*";
        AtomicLong deleteCount = new AtomicLong(0);
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                List<byte[]> batch = new ArrayList<>(1000);
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() >= 1000) {
                        Long deleted = connection.keyCommands().del(batch.toArray(new byte[0][]));
                        deleteCount.addAndGet(Objects.requireNonNullElse(deleted, 0L));
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    Long deleted = connection.keyCommands().del(batch.toArray(new byte[0][]));
                    deleteCount.addAndGet(Objects.requireNonNullElse(deleted, 0L));
                }
            }
            return null;
        });
        return deleteCount.get();
    }

    public Collection<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public Set<String> scanKeys(String pattern, int count) {
        return scanKeys(DataType.STRING, pattern, count);
    }

    public Set<String> scanKeys(DataType dataType, String pattern, int count) {
        if (Objects.isNull(dataType) || DataType.NONE.equals(dataType)) {
            return Set.of();
        }
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            KeyScanOptions options = (KeyScanOptions) KeyScanOptions.scanOptions(dataType)
                    .match(pattern).count(count).build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return keys;
        });
    }
}
