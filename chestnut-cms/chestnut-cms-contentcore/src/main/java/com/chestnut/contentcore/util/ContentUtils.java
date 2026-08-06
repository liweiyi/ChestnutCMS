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
package com.chestnut.contentcore.util;

import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.impl.PublishPipeProp_ContentExTemplate;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.vo.ListContentVO;
import com.chestnut.contentcore.enums.ContentCoreTips;
import com.chestnut.contentcore.fixed.dict.ContentCopyType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContentUtils {

    /**
     * 获取内容扩展模板静态化相对站点路径
     *
     * @return
     */
    public static String getContentExPath(CmsSite site, CmsCatalog catalog, CmsContent content, String publishPipeCode) {
        String suffix = site.getStaticSuffix(publishPipeCode);
        return catalog.getPath() + getContextExFileName(content.getContentId(), suffix);
    }

    public static String getContextExFileName(Long contentId, String suffix) {
        return contentId + "_ex." + suffix;
    }

    public static String getContentExTemplate(CmsContent content, CmsCatalog catalog, String publishPipeCode) {
        String exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode,
                content.getPublishPipeProps());
        if (StringUtils.isEmpty(exTemplate)) {
            // 无内容独立扩展模板取栏目配置
            exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        }
        return exTemplate;
    }

    public static void dealCopyInfo(List<ListContentVO> list, ISiteService siteService,
            ICatalogService catalogService, IContentService contentService) {
        List<Long> copySourceContentIds = list.stream().map(ListContentVO::getCopyId).filter(IdUtils::validate).toList();
        if (copySourceContentIds.isEmpty()) {
            return;
        }
        Map<Long, CmsContent> copySourceContents = contentService.dao().lambdaQuery()
                .select(CmsContent::getContentId, CmsContent::getSiteId, CmsContent::getCatalogId, CmsContent::getTitle)
                .in(CmsContent::getContentId, copySourceContentIds)
                .list().stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(CmsContent::getContentId, c -> c));

        Map<Long, CmsSite> siteMap = new HashMap<>();
        Map<Long, CmsCatalog> catalogMap = new HashMap<>();
        list.forEach(vo -> {
            if (IdUtils.validate(vo.getCopyId())) {
                CmsContent copySourceContent = copySourceContents.get(vo.getCopyId());
                String siteName = null;
                String catalogName = null;
                if (Objects.isNull(copySourceContent)) {
                    siteName = ContentCoreTips.MISSING_COPY_SOURCE.locale();
                } else {
                    CmsSite copySourceSite = siteMap.get(copySourceContent.getSiteId());
                    if (Objects.isNull(copySourceSite)) {
                        copySourceSite = siteService.getSite(copySourceContent.getSiteId());
                        if (Objects.nonNull(copySourceSite)) {
                            siteMap.put(copySourceContent.getSiteId(), copySourceSite);
                            siteName = copySourceSite.getName();
                        } else {
                            siteName = "[Unknown]";
                        }
                    }
                    CmsCatalog copySourceCatalog = catalogMap.get(copySourceContent.getCatalogId());
                    if (Objects.isNull(copySourceCatalog)) {
                        copySourceCatalog = catalogService.getCatalog(copySourceContent.getCatalogId());
                        if (Objects.nonNull(copySourceCatalog)) {
                            catalogMap.put(copySourceContent.getCatalogId(), copySourceCatalog);
                            catalogName = copySourceCatalog.getName();
                        } else {
                            catalogName = "[Unknown]";
                        }
                    }
                }
                ContentCopyType.ContentCopyInfo copyInfo = new ContentCopyType.ContentCopyInfo(vo.getCopyType(),
                        vo.getCopyId(), siteName, catalogName, copySourceContent.getTitle());
                vo.setCopyInfo(copyInfo);
            }
        });
    }
}
