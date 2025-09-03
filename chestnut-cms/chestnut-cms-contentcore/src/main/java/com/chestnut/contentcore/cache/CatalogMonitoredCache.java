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
import com.chestnut.contentcore.domain.CmsCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * CatalogMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + CatalogMonitoredCache.ID)
@RequiredArgsConstructor
public class CatalogMonitoredCache implements IMonitoredCache<CmsCatalog> {

    public static final String ID = "Catalog";

    private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "catalog:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.CATALOG}";
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    @Override
    public CmsCatalog getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, CmsCatalog.class);
    }

    private String cacheKeyById(Long catalogId) {
        return CACHE_PREFIX + "id:" + catalogId;
    }

    private String cacheKeyByAlias(Long siteId, String alias) {
        return CACHE_PREFIX + "alias:" + siteId + ":" + alias;
    }

    public CmsCatalog getCacheById(Long catalogId) {
        return redisCache.getCacheObject(cacheKeyById(catalogId), CmsCatalog.class);
    }

    public CmsCatalog getCacheById(Long catalogId, Supplier<CmsCatalog> supplier) {
        return redisCache.getCacheObject(cacheKeyById(catalogId), CmsCatalog.class, supplier);
    }

    public CmsCatalog getCacheByAlias(Long siteId, String alias) {
        return redisCache.getCacheObject(cacheKeyByAlias(siteId, alias), CmsCatalog.class);
    }

    public CmsCatalog getCacheByAlias(Long siteId, String alias, Supplier<CmsCatalog> supplier) {
        return redisCache.getCacheObject(cacheKeyByAlias(siteId, alias), CmsCatalog.class, supplier);
    }

    public void clear(Long siteId, Long catalogId, String catalogAlias) {
        this.redisCache.deleteObject(cacheKeyById(catalogId));
        this.redisCache.deleteObject(cacheKeyByAlias(siteId, catalogAlias));
    }
}
