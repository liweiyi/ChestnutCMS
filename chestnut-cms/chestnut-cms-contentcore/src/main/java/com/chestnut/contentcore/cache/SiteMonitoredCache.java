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
package com.chestnut.contentcore.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsSite;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * SiteMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + SiteMonitoredCache.ID)
@RequiredArgsConstructor
public class SiteMonitoredCache implements IMonitoredCache<CmsSite> {

    public static final String ID = "Site";

    private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "site:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.SITE}";
    }

    @Override
    public CmsSite getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, CmsSite.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public CmsSite getCache(Long siteId, Supplier<CmsSite> supplier) {
        return redisCache.getCacheObject(CACHE_PREFIX + siteId, CmsSite.class, supplier);
    }

    public void clear(long siteId) {
        this.redisCache.deleteObject(CACHE_PREFIX + siteId);
    }
}
