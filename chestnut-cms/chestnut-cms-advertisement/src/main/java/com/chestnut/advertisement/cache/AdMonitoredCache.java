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
package com.chestnut.advertisement.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.contentcore.config.CMSConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/**
 * AdMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + AdMonitoredCache.ID)
@RequiredArgsConstructor
public class AdMonitoredCache implements IMonitoredCache<Map<String, String>> {

    public static final String ID = "AD";

    private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "adv-ids";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.AD}";
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    @Override
    public Map<String, String> getCache(String cacheKey) {
        return redisCache.getCacheMap(cacheKey, String.class);
    }

    public Map<String, String> getCache(Supplier<Map<String, String>> supplier) {
        return redisCache.getCacheMap(CACHE_PREFIX, String.class, supplier);
    }

    public void clear() {
        this.redisCache.deleteObject(CACHE_PREFIX);
    }
}
