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
package com.chestnut.contentcore.publish.staticize;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.cache.CatalogMonitoredCache;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.PublishPipeProp_DefaultListTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_IndexTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_ListTemplate;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCoreTips;
import com.chestnut.contentcore.properties.MaxPageOnContentPublishProperty;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.CatalogTemplateType;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.template.utility.NullWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * CatalogListStaticizeType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStaticizeType.BEAN_PREFIX + CatalogListStaticizeType.TYPE)
public class CatalogListStaticizeType implements IStaticizeType {

    public static final String TYPE = "cataloglist";

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private final CatalogMonitoredCache catalogMonitoredCache;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void staticize(String dataId) {
        Long catalogId = Long.valueOf(dataId);
        if (IdUtils.validate(catalogId)) {
            CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
            if (Objects.isNull(catalog)) {
                logger.warn("Catalog not found: {}", catalogId);
            }
            this.catalogStaticize(catalog);
        }
    }

    public void catalogStaticize(CmsCatalog catalog) {
        CmsSite site = this.siteService.getSite(catalog.getSiteId());
        // 每次最多静态化maxPage页
        int maxPage = MaxPageOnContentPublishProperty.getValue(site.getConfigProps());
        this.catalogStaticize(catalog, maxPage);
    }

    public void catalogStaticize(CmsCatalog catalog, int pageMax) {
        if (!catalog.isStaticize() || !catalog.isVisible() || CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            return;
        }
        List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
        for (CmsPublishPipe pp : publishPipes) {
            this.doCatalogStaticize(catalog, pp.getCode(), pageMax);
        }
    }

    private void doCatalogStaticize(CmsCatalog catalog, String publishPipeCode, int pageMax) {
        CmsSite site = this.siteService.getSite(catalog.getSiteId());
        if (!catalog.isStaticize()) {
            logger.warn("[{}]The catalog static disabled: {}", publishPipeCode, catalog.getName());
            return;
        }
        if (!catalog.isVisible()) {
            logger.warn("[{}]The catalog invisible: {}", publishPipeCode, catalog.getName());
            return;
        }
        if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            logger.warn("[{}]The link catalog cannot be static: {}", publishPipeCode, catalog.getName());
            return;
        }
        String indexTemplate = PublishPipeProp_IndexTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        String listTemplate = PublishPipeProp_ListTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        if (StringUtils.isEmpty(listTemplate)) {
            listTemplate = PublishPipeProp_DefaultListTemplate.getValue(publishPipeCode, site.getPublishPipeProps()); // 取站点默认模板
        }
        File indexTemplateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
        File listTemplateFile = this.templateService.findTemplateFile(site, listTemplate, publishPipeCode);
        if (Objects.isNull(indexTemplateFile) && Objects.isNull(listTemplate)) {
            logger.warn(AsyncTaskManager.addErrMessage(ContentCoreTips.TEMPLATE_NOT_FOUND,
                    TYPE + "#" + publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName()));
            return;
        }

        String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
        String dirPath = siteRoot + catalog.getPath();
        FileExUtils.mkdirs(dirPath);
        String staticSuffix = site.getStaticSuffix(publishPipeCode); // 静态化文件类型
        // 没有列表模板
        if (Objects.isNull(listTemplateFile)) {
            catalogMonitoredCache.removeCatalogPublishing(catalog.getCatalogId());
            return;
        }
        long s = System.currentTimeMillis();
        try {
            int startPage = catalogMonitoredCache.getAndUpdateCatalogPublishingPageNo(catalog.getCatalogId(), pageMax);
            int endPage = Math.max(startPage, 1) + pageMax - 1;

            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, listTemplate);
            TemplateContext context = new TemplateContext(templateKey, false, publishPipeCode);
            context.setPageIndex(startPage);
            context.setMaxPageNo(endPage);
            context.setDirectory(dirPath);
            String firstFileNameWithoutExtension = (Objects.nonNull(indexTemplateFile) ? "list" : "index");
            context.setFirstFileName(firstFileNameWithoutExtension + StringUtils.DOT + staticSuffix);
            context.setOtherFileName(
                    firstFileNameWithoutExtension + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + staticSuffix);
            // init template variables
            TemplateUtils.initGlobalVariables(site, context);
            // init templateType variables
            ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
            templateType.initTemplateData(catalog.getCatalogId(), context);
            if (startPage == 1) {
                // 第一页必须有
                String pageFilePath = context.getStaticizeFilePath(context.getPageIndex());
                try (FileWriter writer = new FileWriter(pageFilePath, StandardCharsets.UTF_8)) {
                    this.staticizeService.process(context, writer);
                    this.log(site, "[{}]Catalog list template parsed: {}, page: {}, cost: {}ms",
                            publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(), 1, (System.currentTimeMillis() - s));
                }
            } else {
                // 空跑一次初始化PageTotal和PageSize
                this.staticizeService.process(context, NullWriter.INSTANCE);
                context.setPageIndex(context.getPageIndex() - 1);
            }
            while (context.hasNextPage()) {
                s = System.currentTimeMillis();
                context.setPaged(false);
                context.setPageIndex(context.getPageIndex() + 1);
                String pageFilePath = context.getStaticizeFilePath(context.getPageIndex());
                try (FileWriter writer = new FileWriter(pageFilePath, StandardCharsets.UTF_8)) {
                    this.staticizeService.process(context, writer);
                }
                this.log(site, "[{}]Catalog list template parsed: {}, page: {}, cost: {}ms",
                        publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(),
                        context.getPageIndex(), (System.currentTimeMillis() - s));
            }
            if (endPage <= 0 || context.getPageCount() <= endPage) {
                this.log(site, "[{}]Catalog list template publish end: {}, page: {}.",
                        publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(),
                        context.getPageIndex());
                catalogMonitoredCache.removeCatalogPublishing(catalog.getCatalogId());
            }
        } catch (Exception e1) {
            logger.error(AsyncTaskManager.addErrMessage(ContentCoreTips.TEMPLATE_PARSE_FAILED,
                    TYPE + "#" + publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName()), e1);
        }
    }
}
