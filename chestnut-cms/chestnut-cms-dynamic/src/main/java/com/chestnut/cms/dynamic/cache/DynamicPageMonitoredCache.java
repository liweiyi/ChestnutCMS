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
package com.chestnut.cms.dynamic.cache;

import com.chestnut.cms.dynamic.domain.CmsDynamicPage;
import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.contentcore.config.CMSConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * DynamicPageMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + DynamicPageMonitoredCache.ID)
@RequiredArgsConstructor
public class DynamicPageMonitoredCache implements IMonitoredCache<CmsDynamicPage> {

    public static final String ID = "DynamicPage";

    private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "dynamic_page:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.DYNAMIC_PAGE}";
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    @Override
    public CmsDynamicPage getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, CmsDynamicPage.class);
    }

    private String cacheKeyByPath(Long siteId, String path) {
        return CACHE_PREFIX + siteId + ":path:" + path;
    }

    private String cacheKeyByCode(Long siteId, String code) {
        return CACHE_PREFIX + siteId + ":code:" + code;
    }

    public void setCache(CmsDynamicPage dynamicPage) {
        this.redisCache.setCacheObject(cacheKeyByPath(dynamicPage.getSiteId(), dynamicPage.getPath()), dynamicPage);
        this.redisCache.setCacheObject(cacheKeyByCode(dynamicPage.getSiteId(), dynamicPage.getCode()), dynamicPage);
    }

    public void clearCache(CmsDynamicPage dynamicPage) {
        this.redisCache.deleteObject(cacheKeyByPath(dynamicPage.getSiteId(), dynamicPage.getPath()));
        this.redisCache.deleteObject(cacheKeyByCode(dynamicPage.getSiteId(), dynamicPage.getCode()));
    }

    public CmsDynamicPage getCacheByPath(Long siteId, String path, Supplier<CmsDynamicPage> supplier) {
        String cacheKey = cacheKeyByPath(siteId, path);
        return redisCache.getCacheObject(cacheKey, CmsDynamicPage.class, supplier);
    }

    public CmsDynamicPage getCacheByCode(Long siteId, String code, Supplier<CmsDynamicPage> supplier) {
        String cacheKey = cacheKeyByCode(siteId, code);
        return redisCache.getCacheObject(cacheKey, CmsDynamicPage.class, supplier);
    }
}
