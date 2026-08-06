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
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
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

    private static final DefaultRedisScript<Long> UPDATE_CATALOG_PUBLISHING_SCRIPT = new DefaultRedisScript<>("""
            local field = ARGV[1]
            local pages = tonumber(ARGV[2])
            if not pages then
                return redis.error_reply('Catalog publishing pages must be numeric')
            end
            local value = redis.call('HGET', KEYS[1], field)
            local current = tonumber(value) or 1
            redis.call('HSET', KEYS[1], field, current + pages)
            return current
            """, Long.class);

    private static final RedisSerializer<String> STRING_SERIALIZER = RedisSerializer.string();

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

    public Map<Long, CmsCatalog> getCachesById(Collection<Long> catalogIds) {
        if (catalogIds == null || catalogIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = catalogIds.stream().filter(Objects::nonNull).distinct().toList();
        List<String> keys = ids.stream().map(this::cacheKeyById).toList();
        List<CmsCatalog> catalogs = redisCache.getCacheObjects(keys, CmsCatalog.class);
        Map<Long, CmsCatalog> result = new LinkedHashMap<>(catalogs.size());
        for (int i = 0; i < catalogs.size(); i++) {
            CmsCatalog catalog = catalogs.get(i);
            if (catalog != null) {
                result.put(ids.get(i), catalog);
            }
        }
        return result;
    }

    public void setCacheById(Long catalogId, CmsCatalog catalog) {
        this.redisCache.setCacheObject(cacheKeyById(catalogId), catalog);
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

    /**
     * 更新栏目发布进度
     *
     * @param catalogId 栏目ID
     * @param pages 本次发布任务发布的分页数量
     */
    private int updateCatalogPublishing(Long catalogId, int pages) {
        Long pageNo = this.redisCache.getRedisTemplate().execute(
                UPDATE_CATALOG_PUBLISHING_SCRIPT,
                STRING_SERIALIZER,
                null,
                List.of(ContentCoreConsts.CATALOG_PUBLISHING_CACHE_KEY),
                catalogId.toString(),
                String.valueOf(pages));
        return pageNo.intValue();
    }

    public void resetCatalogPublishing(Long catalogId) {
        this.removeCatalogPublishing(catalogId);
        this.updateCatalogPublishing(catalogId, 0);
    }

    /**
     * 删除栏目发布进度
     * @param catalogId
     */
    public void removeCatalogPublishing(Long catalogId) {
        this.redisCache.deleteCacheMapValue(ContentCoreConsts.CATALOG_PUBLISHING_CACHE_KEY, catalogId.toString());
    }

    public int getAndUpdateCatalogPublishingPageNo(Long catalogId, int pages) {
        if (!redisCache.hasMapKey(ContentCoreConsts.CATALOG_PUBLISHING_CACHE_KEY, catalogId.toString())) {
            return -1;
        }
        return this.updateCatalogPublishing(catalogId, pages);
    }

    /**
     * 获取是所有发布进度栏目ID
     */
    public List<Long> getCatalogPublishingKeys() {
        Map<String, Integer> map = this.redisCache.getCacheMap(ContentCoreConsts.CATALOG_PUBLISHING_CACHE_KEY, Integer.class);
        if (Objects.isNull(map)) {
            return List.of();
        }
        return map.keySet().stream().map(Long::valueOf).toList();
    }
}
