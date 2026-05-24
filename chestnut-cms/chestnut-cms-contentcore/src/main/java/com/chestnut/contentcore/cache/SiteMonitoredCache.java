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
package com.chestnut.contentcore.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsSite;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private String cacheKeyById(Long siteId) {
        return CACHE_PREFIX + siteId;
    }

    public CmsSite getCache(Long siteId, Supplier<CmsSite> supplier) {
        return redisCache.getCacheObject(cacheKeyById(siteId), CmsSite.class, supplier);
    }

    public Map<Long, CmsSite> getCaches(Collection<Long> siteIds) {
        if (siteIds == null || siteIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = siteIds.stream().filter(Objects::nonNull).distinct().toList();
        List<String> keys = ids.stream().map(this::cacheKeyById).toList();
        List<CmsSite> sites = redisCache.getCacheObjects(keys, CmsSite.class);
        Map<Long, CmsSite> result = new LinkedHashMap<>(sites.size());
        for (int i = 0; i < sites.size(); i++) {
            CmsSite site = sites.get(i);
            if (site != null) {
                result.put(ids.get(i), site);
            }
        }
        return result;
    }

    public void setCache(Long siteId, CmsSite site) {
        this.redisCache.setCacheObject(cacheKeyById(siteId), site);
    }

    public void clear(long siteId) {
        this.redisCache.deleteObject(cacheKeyById(siteId));
    }
}
