/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.word.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.word.sensitive.SensitiveWordType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

/**
 * ErrorProneWordMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + SensitiveWordMonitoredCache.ID)
@RequiredArgsConstructor
public class SensitiveWordMonitoredCache implements IMonitoredCache<Set<String>> {

    public static final String ID = "SensitiveWord";

    private static final String CACHE_PREFIX = "sensitive_word:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.SENSITIVE_WORD}";
    }

    @Override
    public Set<String> getCache(String cacheKey) {
        return this.redisCache.getCacheSet(cacheKey, String.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public Set<String> get(SensitiveWordType type, Supplier<Set<String>> supplier) {
        String cacheKey = CACHE_PREFIX + type.name().toLowerCase();
        if (!this.redisCache.hasKey(cacheKey)) {
            Set<String> words = supplier.get();
            this.redisCache.addSetValue(cacheKey, words.toArray(String[]::new));
            return words;
        }
        return this.redisCache.getCacheSet(cacheKey, String.class);
    }

    public void clear() {
        for (SensitiveWordType type : SensitiveWordType.values()) {
            this.redisCache.deleteObject(CACHE_PREFIX + type.name().toLowerCase());
        }
    }
}
