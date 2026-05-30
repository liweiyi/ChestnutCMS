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
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.*;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * PageWidgetStaticizeType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStaticizeType.BEAN_PREFIX + PageWidgetStaticizeType.TYPE)
public class PageWidgetStaticizeType implements IStaticizeType {

    public static final String TYPE = "pagewidget";

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private final IPageWidgetService pageWidgetService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void staticize(String dataId) {
        Long pageWidgetId = Long.valueOf(dataId);
        if (IdUtils.validate(pageWidgetId)) {

            CmsPageWidget pageWidget = this.pageWidgetService.getById(pageWidgetId);
            if (Objects.isNull(pageWidget)) {
                logger.warn("PageWidget not found: {}", pageWidgetId);
            }
            this.pageWidgetStaticize(pageWidget);
        }
    }

    public void pageWidgetStaticize(CmsPageWidget pw) {
        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(pw.getSiteId());
        // 发布内容
        CmsSite site = this.siteService.getSite(pw.getSiteId());
        for (CmsPublishPipe pp : publishPipes) {
            String template = pw.getTemplate(pp.getCode());
            File templateFile = this.templateService.findTemplateFile(site, template, pp.getCode());
            if (Objects.nonNull(templateFile)) {
                doStaticize(pw, site, pp.getCode(), template);
            } else {
                logger.warn("[{}]The page-widget template not configured or the file does not exist: {}#{}", pp.getCode(), pw.getName(), pw.getCode());
            }
        }
    }

    private void doStaticize(CmsPageWidget pw, CmsSite site, String publishPipeCode, String template) {
        long s = System.currentTimeMillis();
        try {
            // 静态化目录
            String dirPath = SiteUtils.getSiteRoot(site, publishPipeCode) + pw.getPath();
            FileExUtils.mkdirs(dirPath);
            // 自定义模板上下文
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
            TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
            templateContext.setDirectory(dirPath);
            String staticFileName = PageWidgetUtils.getStaticFileName(pw, site.getStaticSuffix(publishPipeCode));
            templateContext.setFirstFileName(staticFileName);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            templateContext.getVariables().put(TemplateUtils.TemplateVariable_PageWidget, pw);
            // init templateType data to datamode
            ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
            templateType.initTemplateData(site.getSiteId(), templateContext);
            // staticize
            this.staticizeService.process(templateContext);
            this.log(site, "[{}]The page-widget template parsed: {}, cost: {}ms", publishPipeCode, pw.getCode(), System.currentTimeMillis() - s);
        } catch (TemplateException | IOException e) {
            logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]The page-widget template parse failed: {1}#{2}",
                    publishPipeCode, pw.getName(), pw.getCode())), e);
        }
    }
}
