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
package com.chestnut.cms.dynamic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.cms.dynamic.core.IDynamicPageInitData;
import com.chestnut.cms.dynamic.domain.CmsDynamicPage;
import com.chestnut.cms.dynamic.mapper.CmsDynamicPageMapper;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IDynamicPageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicPageHelper {

    private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "dynamic_page:";

    private final RedisCache redisCache;

    private final Map<String, IDynamicPageInitData> dynamicPageInitDataMap;

    private final Map<String, IDynamicPageType> dynamicPageTypeMap;

    private final CmsDynamicPageMapper dynamicPageMapper;

    public IDynamicPageInitData getDynamicPageInitData(String type) {
        return dynamicPageInitDataMap.get(IDynamicPageInitData.BEAN_PREFIX + type);
    }

    public String getDynamicPagePath(Long siteId, String code) {
        IDynamicPageType dynamicPageType = this.dynamicPageTypeMap.get(IDynamicPageType.BEAN_PREFIX + code);
        if (Objects.nonNull(dynamicPageType)) {
            return dynamicPageType.getRequestPath();
        }
        CmsDynamicPage dynamicPage = getDynamicPageByCode(siteId, code);
        if (Objects.nonNull(dynamicPage)) {
            return dynamicPage.getPath();
        }
        return null;
    }

    public void clearCache(CmsDynamicPage dynamicPage) {
        this.redisCache.deleteObject(CACHE_PREFIX + dynamicPage.getSiteId() + ":" + dynamicPage.getPath());
        this.redisCache.deleteObject(CACHE_PREFIX + dynamicPage.getSiteId() + ":" + dynamicPage.getCode());
    }

    public void updateCache(CmsDynamicPage dynamicPage) {
        this.redisCache.setCacheObject(CACHE_PREFIX + dynamicPage.getSiteId() + ":" + dynamicPage.getPath(), dynamicPage);
        this.redisCache.setCacheObject(CACHE_PREFIX + dynamicPage.getSiteId() + ":" + dynamicPage.getCode(), dynamicPage);
    }

    public CmsDynamicPage getDynamicPageByPath(Long siteId, String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        LambdaQueryWrapper<CmsDynamicPage> q = new LambdaQueryWrapper<CmsDynamicPage>()
                .eq(CmsDynamicPage::getSiteId, siteId)
                .eq(CmsDynamicPage::getPath, path);
        return redisCache.getCacheObject(CACHE_PREFIX + siteId + ":" + path, () ->
            this.dynamicPageMapper.selectOne(q)
        );
    }

    public CmsDynamicPage getDynamicPageByCode(Long siteId, String code) {
        LambdaQueryWrapper<CmsDynamicPage> q = new LambdaQueryWrapper<CmsDynamicPage>()
                .eq(CmsDynamicPage::getSiteId, siteId)
                .eq(CmsDynamicPage::getCode, code);
        return redisCache.getCacheObject(CACHE_PREFIX + siteId + ":" + code, () ->
                this.dynamicPageMapper.selectOne(q)
        );
    }
}
