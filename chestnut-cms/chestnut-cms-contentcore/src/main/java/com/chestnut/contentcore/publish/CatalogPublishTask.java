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
package com.chestnut.contentcore.publish;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.config.CMSPublishConfig;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.PublishPipeProp_DefaultListTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_IndexTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_ListTemplate;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.MaxPageOnContentPublishProperty;
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
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 栏目发布任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IPublishTask.BeanPrefix + CatalogPublishTask.Type)
public class CatalogPublishTask implements IPublishTask<CmsCatalog> {

    public final static String Type = "catalog";

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private final StringRedisTemplate redisTemplate;

    @Override
    public String getType() {
        return Type;
    }

    @Override
    public void publish(CmsCatalog catalog) {
        if (!catalog.isStaticize() || !catalog.isVisible() || CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            return;
        }
        String dataId = catalog.getCatalogId().toString();
        MapRecord<String, String, String> record = MapRecord.create(CMSPublishConfig.PublishStreamName, Map.of(
                "type", Type,
                "id", dataId
        )).withId(RecordId.of(Instant.now().toEpochMilli(), catalog.getCatalogId()));
        redisTemplate.opsForStream().add(record);
    }

    @Override
    public void staticize(Map<String, String> dataMap) {
        Long catalogId = MapUtils.getLong(dataMap, "id");
        if (IdUtils.validate(catalogId)) {
            CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
            if (Objects.nonNull(catalog)) {
                this.catalogStaticize(catalog);
            }
        }
    }
    public void catalogStaticize(CmsCatalog catalog) {
        CmsSite site = this.siteService.getSite(catalog.getSiteId());
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
            logger.warn("【{}】未启用静态化的栏目跳过静态化：{}", publishPipeCode, catalog.getName());
            return;
        }
        if (!catalog.isVisible()) {
            logger.warn("【{}】不可见状态的栏目跳过静态化：{}", publishPipeCode, catalog.getName());
            return;
        }
        if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            logger.warn("【{}】链接类型栏目跳过静态化：{}", publishPipeCode, catalog.getName());
            return;
        }
        String indexTemplate = PublishPipeProp_IndexTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        String listTemplate = PublishPipeProp_ListTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        if (StringUtils.isEmpty(listTemplate)) {
            listTemplate = PublishPipeProp_DefaultListTemplate.getValue(publishPipeCode, site.getPublishPipeProps()); // 取站点默认模板
        }
        File indexTemplateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
        File listTemplateFile = this.templateService.findTemplateFile(site, listTemplate, publishPipeCode);
        if (indexTemplateFile == null && listTemplateFile == null) {
            logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目首页模板和列表页模板未配置或不存在：{1}",
                    publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())));
            return;
        }
        String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
        String dirPath = siteRoot + catalog.getPath();
        FileExUtils.mkdirs(dirPath);
        String staticSuffix = site.getStaticSuffix(publishPipeCode); // 静态化文件类型

        // 发布栏目首页
        long s = System.currentTimeMillis();
        if (Objects.nonNull(indexTemplateFile)) {
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
                logger.debug("[{}]栏目首页模板解析：{}，耗时：{}ms", publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(), (System.currentTimeMillis() - s));
            } catch (IOException | TemplateException e) {
                logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目首页解析失败：{1}",
                        publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())), e);
            }
        }
        // 发布栏目列表页
        if (Objects.nonNull(listTemplateFile)) {
            s = System.currentTimeMillis();
            try {
                String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, listTemplate);
                TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
                templateContext.setMaxPageNo(pageMax);
                templateContext.setDirectory(dirPath);
                String name = Objects.nonNull(indexTemplateFile) ? "list" : "index";
                templateContext.setFirstFileName(name + StringUtils.DOT + staticSuffix);
                templateContext.setOtherFileName(
                        name + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + staticSuffix);
                // init template variables
                TemplateUtils.initGlobalVariables(site, templateContext);
                // init templateType variables
                ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
                templateType.initTemplateData(catalog.getCatalogId(), templateContext);
                // staticize
                this.staticizeService.process(templateContext);
                logger.debug("[{}]栏目列表模板解析：{}，耗时：{}ms", publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(), (System.currentTimeMillis() - s));
            } catch (Exception e1) {
                logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目列表页解析失败：{1}",
                        publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())), e1);
            }
        }
    }
}
