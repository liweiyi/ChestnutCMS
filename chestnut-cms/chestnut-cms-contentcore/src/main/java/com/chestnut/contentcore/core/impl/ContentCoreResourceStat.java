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
        this.contentService.lambdaQuery().select(List.of(CmsContent::getLogo)).list().forEach(content -> {
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
