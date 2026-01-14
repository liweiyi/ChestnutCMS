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
package com.chestnut.contentcore.publish.staticize;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.publish.IContentPathRule;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.ContentTemplateType;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.ContentUtils;
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
 * ContentStaticizeType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStaticizeType.BEAN_PREFIX + ContentStaticizeType.TYPE)
public class ContentStaticizeType implements IStaticizeType {

    public static final String TYPE = "content";

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void staticize(String dataId) {
        Long contentId = Long.valueOf(dataId);
        if (IdUtils.validate(contentId)) {
            CmsContent content = this.contentService.dao().getById(contentId);
            if (Objects.isNull(content)) {
                logger.warn("Content not found: {}", contentId);
            }
            this.contentStaticize(content);
        }
    }

    public void contentStaticize(CmsContent cmsContent) {
        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(cmsContent.getSiteId());
        // 发布内容
        for (CmsPublishPipe pp : publishPipes) {
            doContentStaticize(cmsContent, pp.getCode());
            // 内容扩展模板静态化
            doContentExStaticize(cmsContent, pp.getCode());
        }
    }

    private void doContentStaticize(CmsContent content, String publishPipeCode) {
        CmsSite site = this.siteService.getSite(content.getSiteId());
        CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
        if (!catalog.isStaticize()) {
            logger.warn("[ {} ]栏目设置不静态化[ {}#{} ]：{}", publishPipeCode, site.getName(),
                    catalog.getName(), content.getTitle());
            return; // 不静态化直接跳过
        }
        if (content.isLinkContent()) {
            logger.warn("[ {} ]标题内容不需要静态化[ {}#{} ]：{}", publishPipeCode, site.getName(),
                    catalog.getName(), content.getTitle());
            return; // 标题内容不需要静态化
        }
        final String detailTemplate = TemplateUtils.getDetailTemplate(site, catalog, content, publishPipeCode);
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
        if (templateFile == null) {
            logger.warn(AsyncTaskManager.addErrMessage(
                    StringUtils.messageFormat("[ {0} ]内容模板未设置或文件不存在[ {1}#{2} ]：{3}",
                            publishPipeCode, site.getName(), catalog.getName(), content.getTitle())));
            return;
        }
        try {
            long s = System.currentTimeMillis();
            // 自定义模板上下文
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, detailTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
            templateType.initTemplateData(content.getContentId(), templateContext);
            // 静态化文件地址
            this.setContentStaticPath(site, catalog, content, templateContext);
            // 静态化
            this.staticizeService.process(templateContext);
            logger.debug("[ {} ]内容详情页模板解析[ {}#{} ]：{}，耗时：{}ms", publishPipeCode, site.getName(),
                    catalog.getName(), content.getTitle(), (System.currentTimeMillis() - s));
        } catch (TemplateException | IOException e) {
            logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]内容详情页解析失败：[{1}]{2}",
                    publishPipeCode, catalog.getName(), content.getTitle())), e);
        }
    }

    private void setContentStaticPath(CmsSite site, CmsCatalog catalog, CmsContent content, TemplateContext context) {
        String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
        if (StringUtils.isNotBlank(content.getStaticPath())) {
            String dir = "";
            String filename = content.getStaticPath();
            if (filename.contains("/")) {
                int index = filename.lastIndexOf("/");
                dir = filename.substring(0, index + 1);
                filename = filename.substring(index + 1);
            }
            context.setDirectory(siteRoot + dir);
            context.setFirstFileName(filename);
            String name = filename;
            String suffix = "";
            if (filename.contains(".")) {
                int index = filename.lastIndexOf(".");
                name = filename.substring(0, index);
                suffix = filename.substring(index);
            }
            context.setOtherFileName(name + "_" + TemplateContext.PlaceHolder_PageNo + suffix);
        } else {
            IContentPathRule rule = ContentCoreUtils.getContentPathRule(catalog.getDetailNameRule());
            String path = Objects.isNull(rule) ? catalog.getPath() : rule.getDirectory(site, catalog, content);
            context.setDirectory(siteRoot + path);
            String suffix = site.getStaticSuffix(context.getPublishPipeCode());
            context.setFirstFileName(content.getContentId() + StringUtils.DOT + suffix);
            context.setOtherFileName(
                    content.getContentId() + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + suffix);
        }
    }

    private void doContentExStaticize(CmsContent content, String publishPipeCode) {
        CmsSite site = this.siteService.getSite(content.getSiteId());
        CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
        if (!catalog.isStaticize()) {
            logger.warn("[{}]栏目设置不静态化[{}#{}]：{}", publishPipeCode, site.getName(), catalog.getName(), content.getTitle());
            return; // 不静态化直接跳过
        }
        if (content.isLinkContent()) {
            logger.warn("[{}]标题内容不需要静态化[ {}#{} ]：{}", publishPipeCode, site.getName(), catalog.getName(), content.getTitle());
            return; // 标题内容不需要静态化
        }
        String exTemplate = ContentUtils.getContentExTemplate(content, catalog, publishPipeCode);
        if (StringUtils.isEmpty(exTemplate)) {
            return; // 未设置扩展模板直接跳过
        }
        File templateFile = this.templateService.findTemplateFile(site, exTemplate, publishPipeCode);
        if (templateFile == null) {
            logger.warn("[{}]内容扩展模板未设置或文件不存在[ {}#{} ]：{}", publishPipeCode, site.getName(), catalog.getName(), content.getTitle());
            return;
        }
        try {
            long s = System.currentTimeMillis();
            // 自定义模板上下文
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, exTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
            templateType.initTemplateData(content.getContentId(), templateContext);
            // 静态化文件地址
            String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
            IContentPathRule rule = ContentCoreUtils.getContentPathRule(catalog.getDetailNameRule());
            String path = Objects.isNull(rule) ? catalog.getPath() : rule.getDirectory(site, catalog, content);
            templateContext.setDirectory(siteRoot + path);
            String fileName = ContentUtils.getContextExFileName(content.getContentId(), site.getStaticSuffix(publishPipeCode));
            templateContext.setFirstFileName(fileName);
            // 静态化
            this.staticizeService.process(templateContext);
            logger.debug("[{}]内容扩展模板解析[ {}#{} ]：{}，耗时：{}ms", publishPipeCode, site.getName(),
                    catalog.getName(), content.getTitle(), (System.currentTimeMillis() - s));
        } catch (TemplateException | IOException e) {
            logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}] 内容扩展模板解析失败 [{1}#{2}]：{3}",
                    publishPipeCode, site.getName(), catalog.getName(), content.getTitle())), e);
        }
    }
}
