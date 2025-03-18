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
package com.chestnut.contentcore.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.contentcore.config.CMSConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * TemplateMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + TemplateMonitoredCache.ID)
@RequiredArgsConstructor
public class TemplateMonitoredCache implements IMonitoredCache<String> {

    public static final String ID = "Template";

    private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "template:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.TEMPLATE}";
    }

    @Override
    public String getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, String.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public String getCache(String templateKey, Supplier<String> supplier) {
        return redisCache.getCacheObject(CACHE_PREFIX + templateKey, String.class, supplier);
    }

    public void clear(String templateKey) {
        this.redisCache.deleteObject(CACHE_PREFIX + templateKey);
    }

    public void setCache(String templateKey, String staticContent, int timeout, TimeUnit timeUnit) {
        redisCache.setCacheObject(CACHE_PREFIX + templateKey, staticContent, timeout, timeUnit);
    }
}
