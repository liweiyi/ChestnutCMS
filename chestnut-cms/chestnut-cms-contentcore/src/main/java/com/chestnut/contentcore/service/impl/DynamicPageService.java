package com.chestnut.contentcore.service.impl;

import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.core.IDynamicPageType;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicPageService {

    private final Map<String, IDynamicPageType> dynamicPageTypeMap;

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private IDynamicPageType getDynamicPageType(String type) {
        return dynamicPageTypeMap.get(IDynamicPageType.BEAN_PREFIX + type);
    }

    public void generateDynamicPage(String dynamicPageType, Long siteId, String publishPipeCode, Boolean preview,
                                    Map<String, String> parameters, HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        IDynamicPageType dpt = this.getDynamicPageType(dynamicPageType);
        String template = this.publishPipeService.getPublishPipePropValue(dpt.getPublishPipeKey(), publishPipeCode, site.getPublishPipeProps());
        File templateFile = this.templateService.findTemplateFile(site, template, publishPipeCode);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, new RuntimeException("Template not found: " + template));
            return;
        }
        long s = System.currentTimeMillis();
        try {
            // 校验输入参数
            dpt.validate(parameters);
            // 生成静态页面
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            dpt.initTemplateData(parameters, templateContext);
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员登录页模板解析，耗时：{} ms", System.currentTimeMillis() - s);
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
