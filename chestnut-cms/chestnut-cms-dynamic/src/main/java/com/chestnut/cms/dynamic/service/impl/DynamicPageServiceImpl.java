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
package com.chestnut.cms.dynamic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.cms.dynamic.controller.front.DynamicPageFrontController;
import com.chestnut.cms.dynamic.core.DynamicPageRequestMappingHandlerMapping;
import com.chestnut.cms.dynamic.core.IDynamicPageInitData;
import com.chestnut.cms.dynamic.domain.CmsDynamicPage;
import com.chestnut.cms.dynamic.mapper.CmsDynamicPageMapper;
import com.chestnut.cms.dynamic.service.IDynamicPageService;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicPageServiceImpl extends ServiceImpl<CmsDynamicPageMapper, CmsDynamicPage>
        implements IDynamicPageService, CommandLineRunner {

    private final ISiteService siteService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private final List<RequestMappingHandlerMapping> allRequestMapping;

    private final DynamicPageRequestMappingHandlerMapping dynamicPageRequestMapping;

    private final DynamicPageHelper dynamicPageHelper;


    @Override
    public void addDynamicPage(CmsDynamicPage dynamicPage) {
        if (dynamicPage.getPath().startsWith("/")) {
            dynamicPage.setPath(dynamicPage.getPath().substring(1));
        }
        this.checkDynamicPage(dynamicPage);

        dynamicPage.setPageId(IdUtils.getSnowflakeId());
        this.save(dynamicPage);

        this.registerDynamicPageMapping(dynamicPage);

        dynamicPageHelper.updateCache(dynamicPage);
    }

    @Override
    public void saveDynamicPage(CmsDynamicPage dynamicPage) {
        this.checkDynamicPage(dynamicPage);

        CmsDynamicPage dbDynamicPage = this.getById(dynamicPage.getPageId());
        Assert.notNull(dbDynamicPage, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageId", dynamicPage.getPageId()));

        dbDynamicPage.setName(dynamicPage.getName());
        dbDynamicPage.setCode(dynamicPage.getCode());
        dbDynamicPage.setDescription(dynamicPage.getDescription());
        dbDynamicPage.setInitDataTypes(dynamicPage.getInitDataTypes());
        dbDynamicPage.setTemplates(dynamicPage.getTemplates());
        this.updateById(dbDynamicPage);

        dynamicPageHelper.updateCache(dbDynamicPage);
    }

    @Override
    public void deleteDynamicPage(List<Long> dynamicPageIds) {
        List<CmsDynamicPage> dynamicPages = this.listByIds(dynamicPageIds);
        this.removeByIds(dynamicPages);

        dynamicPages.forEach(dynamicPage -> {
            this.unregisterDynamicPageMapping(dynamicPage);
            dynamicPageHelper.clearCache(dynamicPage);
        });
    }

    private void checkDynamicPage(CmsDynamicPage dynamicPage) {
        Long count = this.lambdaQuery()
                .and(wrapper -> wrapper.eq(CmsDynamicPage::getPath, dynamicPage.getPath())
                        .or().eq(CmsDynamicPage::getCode, dynamicPage.getCode()))
                .ne(IdUtils.validate(dynamicPage.getPageId()), CmsDynamicPage::getPageId, dynamicPage.getPageId())
                .count();
        Assert.isTrue(count == 0, () -> CommonErrorCode.DATA_CONFLICT.exception(dynamicPage.getPath() + "||" + dynamicPage.getPath()));
        // 校验路径是冲突
        if (!IdUtils.validate(dynamicPage.getPageId())) {
            for (RequestMappingHandlerMapping mapping : allRequestMapping) {
                Set<RequestMappingInfo> requestMappingInfos = mapping.getHandlerMethods().keySet();
                for (RequestMappingInfo requestMappingInfo : requestMappingInfos) {
                    Assert.isFalse(requestMappingInfo.getPatternValues().contains(dynamicPage.getPath()),
                            () -> new GlobalException("Conflict request handler mapping: " + dynamicPage.getPath()));
                }
            }
        }
    }

    private void registerDynamicPageMapping(CmsDynamicPage dynamicPage) {
        try {
            RequestMappingInfo.Builder builder = RequestMappingInfo
                    .paths(dynamicPage.getPath())
                    .methods(RequestMethod.GET)
                    .mappingName(dynamicPage.getCode());
            RequestMappingInfo mappingInfo = builder.options(this.dynamicPageRequestMapping.getBuilderConfiguration()).build();

            DynamicPageFrontController handler = SpringUtils.getBean(DynamicPageFrontController.class);
            Optional<Method> opt = Arrays.stream(handler.getClass().getMethods())
                    .filter(method -> method.getName().equals("handleDynamicPageRequest")).findFirst();
            opt.ifPresent(method -> this.dynamicPageRequestMapping.registerMapping(mappingInfo, handler, method));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void unregisterDynamicPageMapping(CmsDynamicPage dynamicPage) {
        try {
            RequestMappingInfo.Builder builder = RequestMappingInfo
                    .paths(dynamicPage.getPath())
                    .methods(RequestMethod.GET)
                    .mappingName(dynamicPage.getCode());
            RequestMappingInfo mappingInfo = builder.options(this.dynamicPageRequestMapping.getBuilderConfiguration()).build();

            this.dynamicPageRequestMapping.unregisterMapping(mappingInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化DynamicPageRequestMapping
        this.list().forEach(dynamicPage -> {
            dynamicPageHelper.updateCache(dynamicPage);

            this.registerDynamicPageMapping(dynamicPage);
        });
    }

    public void generateDynamicPage(String requestURI, Long siteId, String publishPipeCode, Boolean preview,
                                    Map<String, String> parameters, HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        CmsDynamicPage dynamicPage = dynamicPageHelper.getDynamicPageByPath(siteId, requestURI);
        String template = dynamicPage.getTemplates().get(publishPipeCode);
        File templateFile = this.templateService.findTemplateFile(site, template, publishPipeCode);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, new RuntimeException("Template not found: " + template));
            return;
        }
        long s = System.currentTimeMillis();
        try {
            // TODO 校验输入参数

            // 生成静态页面
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put(TemplateUtils.TemplateVariable_Request, parameters);
            // 动态页面自定义数据
            if (Objects.nonNull(dynamicPage.getInitDataTypes())) {
                dynamicPage.getInitDataTypes().forEach(initDataType -> {
                    IDynamicPageInitData initData = dynamicPageHelper.getDynamicPageInitData(initDataType);
                    if (Objects.nonNull(initData)) {
                        initData.initTemplateData(templateContext, parameters);
                    }
                });
            }
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("动态模板解析，耗时：{} ms", System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, e);
        }
    }

    private void catchException(String redirectLink, HttpServletResponse response, Exception e) throws IOException {
        if (log.isDebugEnabled()) {
            e.printStackTrace(response.getWriter());
        } else {
            response.sendRedirect(redirectLink); // TODO 通过发布通道属性配置错误页面
        }
    }
}
