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
package com.chestnut.customform;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.PageWidgetUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.mapper.CustomFormMapper;
import com.chestnut.customform.service.ICustomFormService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * CustomFormStaticizeType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStaticizeType.BEAN_PREFIX + CustomFormStaticizeType.TYPE)
public class CustomFormStaticizeType implements IStaticizeType {

    public static final String TYPE = "customform";

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private final CustomFormMapper customFormMapper;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void staticize(String dataId) {
        Long pageWidgetId = Long.valueOf(dataId);
        if (IdUtils.validate(pageWidgetId)) {

            CmsCustomForm form = this.customFormMapper.selectById(pageWidgetId);
            if (Objects.isNull(form)) {
                logger.warn("Custom form not found: {}", pageWidgetId);
            }
            this.staticize(form);
        }
    }

    public void staticize(CmsCustomForm form) {
        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(form.getSiteId());
        CmsSite site = this.siteService.getSite(form.getSiteId());
        for (CmsPublishPipe pp : publishPipes) {
            String template = form.getTemplates().get(pp.getCode());
            File templateFile = this.templateService.findTemplateFile(site, template, pp.getCode());
            if (Objects.nonNull(templateFile)) {
                doStaticize(form, site, pp.getCode(), template);
            } else {
                logger.warn("[{}]The custom form template not configured or the file does not exist: {}#{}", pp.getCode(), form.getName(), form.getCode());
            }
        }
    }

    private void doStaticize(CmsCustomForm form, CmsSite site, String publishPipeCode, String template) {
        long s = System.currentTimeMillis();
        try {
            // 静态化目录
            String dirPath = SiteUtils.getSiteRoot(site, publishPipeCode) + CustomFormConsts.STATICIZE_DIRECTORY;
            FileExUtils.mkdirs(dirPath);
            // 自定义模板上下文
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
            TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
            templateContext.setDirectory(dirPath);
            // 静态化文件地址
            String fileName = form.getCode() + "." + site.getStaticSuffix(publishPipeCode);
            templateContext.setFirstFileName(fileName);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
            templateType.initTemplateData(form.getSiteId(), templateContext);
            templateContext.getVariables().put(CustomFormConsts.TemplateVariable_CustomForm,
                    CustomFormConsts.getCustomFormVariables(form, site, publishPipeCode));
            // 静态化
            this.staticizeService.process(templateContext);
            this.log(site, "[{}]The custom form template parsed: {}, cost: {}ms", publishPipeCode, form.getCode(), System.currentTimeMillis() - s);
        } catch (TemplateException | IOException e) {
            logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]The custom form template parse failed: {1}#{2}",
                    publishPipeCode, form.getName(), form.getCode())), e);
        }
    }
}
