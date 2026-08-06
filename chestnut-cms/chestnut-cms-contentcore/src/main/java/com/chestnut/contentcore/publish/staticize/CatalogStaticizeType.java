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
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.PublishPipeProp_IndexTemplate;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCoreTips;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.CatalogTemplateType;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * CatalogStaticizeType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStaticizeType.BEAN_PREFIX + CatalogStaticizeType.TYPE)
public class CatalogStaticizeType implements IStaticizeType {

    public static final String TYPE = "catalog";

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

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
        this.catalogStaticize(site, catalog);
    }

    public void catalogStaticize(CmsSite site, CmsCatalog catalog) {
        if (!catalog.isStaticize() || !catalog.isVisible() || CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            return;
        }
        List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
        for (CmsPublishPipe pp : publishPipes) {
            this.doCatalogStaticize(site, catalog, pp.getCode());
        }
    }

    private void doCatalogStaticize(CmsSite site, CmsCatalog catalog, String publishPipeCode) {
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
        File indexTemplateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
        if (Objects.isNull(indexTemplateFile)) {
            logger.warn(AsyncTaskManager.addErrMessage(ContentCoreTips.TEMPLATE_NOT_FOUND,
                    TYPE + "#" + publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName()));
            return;
        }

        String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
        String dirPath = siteRoot + catalog.getPath();
        FileExUtils.mkdirs(dirPath);
        String staticSuffix = site.getStaticSuffix(publishPipeCode); // 静态化文件类型

        // 发布栏目首页
        long s = System.currentTimeMillis();
        try {
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, indexTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
            templateContext.setDirectory(dirPath);
            templateContext.setFirstFileName("index" + StringUtils.DOT + staticSuffix);
            // init template variables
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType variables
            ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
            templateType.initTemplateData(catalog.getCatalogId(), templateContext);
            // staticize
            this.staticizeService.process(templateContext);
            this.log(site, "[{}]Catalog index template parsed: {}, cost: {}ms",
                    publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(), (System.currentTimeMillis() - s));
        } catch (IOException | TemplateException e) {
            logger.error(AsyncTaskManager.addErrMessage(ContentCoreTips.TEMPLATE_PARSE_FAILED,
                    TYPE + "#" + publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName()), e);
        }
    }
}
