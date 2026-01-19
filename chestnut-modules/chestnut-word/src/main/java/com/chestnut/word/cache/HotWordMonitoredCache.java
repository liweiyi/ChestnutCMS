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
package com.chestnut.word.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/**
 * HotWordMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + HotWordMonitoredCache.ID)
@RequiredArgsConstructor
public class HotWordMonitoredCache implements IMonitoredCache<Map<String, HotWordMonitoredCache.HotWordCache>> {

    public static final String ID = "HotWord";

    private static final String CACHE_PREFIX = "cc:hotword:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.HOT_WORD}";
    }

    @Override
    public Map<String, HotWordCache> getCache(String cacheKey) {
        return this.redisCache.getCacheMap(cacheKey, HotWordCache.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public static String cacheKey(String groupCode) {
        return CACHE_PREFIX + groupCode;
    }

    public Map<String, HotWordCache> getCache(String groupCode, Supplier<Map<String, HotWordCache>> supplier) {
        return this.redisCache.getCacheMap(cacheKey(groupCode), HotWordCache.class, supplier);
    }

    public void deleteCache(String groupCode) {
        this.redisCache.deleteObject(cacheKey(groupCode));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HotWordCache {
        private String word;
        private String url;
        private String target;
    }
}
