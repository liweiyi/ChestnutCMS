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
package com.chestnut.contentcore.core.impl;

import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class ContentCoreResourceStat implements IResourceStat {

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IContentService contentService;

    @Override
    public Map<Long, Integer> findQuotedResource() {
        Map<Long, Integer> quotedResources = new HashMap<>();
        this.siteLogo().forEach(rid -> {
            quotedResources.compute(rid, (k, v) -> {
                return Objects.isNull(v) ? 1 : v++;
            });
        });

        this.catalogLogo();
        this.contentLogo();
        return quotedResources;
    }

    /**
     * 内容logo引用
     */
    private Set<Long> contentLogo() {
        Set<Long> resourceIds = new HashSet<>();
        this.contentService.dao().lambdaQuery().select(List.of(CmsContent::getLogo)).list().forEach(content -> {
            InternalURL internalURL = InternalUrlUtils.parseInternalUrl(content.getLogo());
            if (Objects.nonNull(internalURL)) {
                resourceIds.add(internalURL.getId());
            }
        });
        return resourceIds;
    }

    /**
     * 栏目logo引用
     */
    private Set<Long> catalogLogo() {
        Set<Long> resourceIds = new HashSet<>();
        this.catalogService.lambdaQuery().select(List.of(CmsCatalog::getLogo)).list().forEach(catalog -> {
            InternalURL internalURL = InternalUrlUtils.parseInternalUrl(catalog.getLogo());
            if (Objects.nonNull(internalURL)) {
                resourceIds.add(internalURL.getId());
            }
        });
        return resourceIds;
    }

    /**
     * 站点logo引用
     */
    private Set<Long> siteLogo() {
        Set<Long> resourceIds = new HashSet<>();
        this.siteService.lambdaQuery().select(List.of(CmsSite::getLogo)).list().forEach(site -> {
            if (InternalUrlUtils.isInternalUrl(site.getLogo())) {
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(site.getLogo());
                resourceIds.add(internalURL.getId());
            }
        });
        return resourceIds;
    }
}
