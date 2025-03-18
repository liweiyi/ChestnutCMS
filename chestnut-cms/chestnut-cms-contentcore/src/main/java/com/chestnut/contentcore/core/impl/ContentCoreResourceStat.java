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
package com.chestnut.contentcore.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.ArrayUtils;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceStat.BEAN_PREFIX + ContentCoreResourceStat.TYPE)
public class ContentCoreResourceStat implements IResourceStat {

    public static final String TYPE = "ContentCore";

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IContentService contentService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        this.siteLogo(siteId, quotedResources);
        this.catalogLogo(siteId, quotedResources);
        this.contentLogo(siteId, quotedResources);
    }

    /**
     * 内容logo引用
     */
    private void contentLogo(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastContentId = 0L;
        int count = 0;
        long total = contentService.dao().lambdaQuery().select(List.of(CmsContent::getImages))
                .eq(CmsContent::getSiteId, siteId).isNotNull(CmsContent::getImages).count();
        while (true) {
            LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
                    .select(List.of(CmsContent::getImages))
                    .eq(CmsContent::getSiteId, siteId)
                    .isNotNull(CmsContent::getImages)
                    .gt(CmsContent::getContentId, lastContentId)
                    .orderByAsc(CmsContent::getContentId);
            Page<CmsContent> page = contentService.dao().page(new Page<>(0, pageSize, false), q);
            for (CmsContent content : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计内容LOGO资源引用：" + count + " / " + total + "]");
                lastContentId = content.getContentId();
                ArrayUtils.mapNotNull(content.getImages(), InternalUrlUtils::parseInternalUrl)
                    .forEach(internalURL -> {
                        quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                    });
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }

    /**
     * 栏目logo引用
     */
    private void catalogLogo(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastCatalogId = 0L;
        int count = 0;
        long total = catalogService.lambdaQuery().select(List.of(CmsCatalog::getLogo))
                .eq(CmsCatalog::getSiteId, siteId).isNotNull(CmsCatalog::getLogo).count();
        while (true) {
            LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>()
                    .select(List.of(CmsCatalog::getLogo))
                    .eq(CmsCatalog::getSiteId, siteId)
                    .isNotNull(CmsCatalog::getLogo)
                    .gt(CmsCatalog::getCatalogId, lastCatalogId)
                    .orderByAsc(CmsCatalog::getCatalogId);
            Page<CmsCatalog> page = catalogService.page(new Page<>(0, pageSize, false), q);
            for (CmsCatalog catalog : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计栏目LOGO资源引用：" + count + " / " + total + "]");
                lastCatalogId = catalog.getCatalogId();
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(catalog.getLogo());
                if (Objects.nonNull(internalURL)) {
                    quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                }
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }

    /**
     * 站点logo引用
     */
    private void siteLogo(Long siteId, Map<Long, Long> quotedResources) {
        CmsSite site = this.siteService.getById(siteId);
        InternalURL internalURL = InternalUrlUtils.parseInternalUrl(site.getLogo());
        if (Objects.nonNull(internalURL)) {
            quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
        }
    }
}
