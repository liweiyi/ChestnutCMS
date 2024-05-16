package com.chestnut.contentcore.publish.staticize;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * SiteStaticizeType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IStaticizeType.BEAN_PREFIX + SiteStaticizeType.TYPE)
public class SiteStaticizeType implements IStaticizeType {

    public static final String TYPE = "site";

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void staticize(String dataId) {
        Long siteId = Long.valueOf(dataId);
        if (IdUtils.validate(siteId)) {
            CmsSite site = this.siteService.getSite(siteId);
            if (Objects.nonNull(site)) {
                this.siteStaticize(site);
            }
        }
    }

    public void siteStaticize(CmsSite site) {
        this.publishPipeService.getPublishPipes(site.getSiteId())
                .forEach(pp -> doSiteStaticize(site, pp.getCode()));
    }

    private void doSiteStaticize(CmsSite site, String publishPipeCode) {
        try {
            AsyncTaskManager
                    .setTaskMessage(StringUtils.messageFormat("[{0}]正在发布站点首页：{1}", publishPipeCode, site.getName()));

            String indexTemplate = site.getIndexTemplate(publishPipeCode);
            File templateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
            if (Objects.isNull(templateFile)) {
                logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]站点首页模板未配置或不存在：{1}",
                        publishPipeCode, site.getSiteId() + "#" + site.getName())));
                return;
            }
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, indexTemplate);
            TemplateContext context = new TemplateContext(templateKey, false, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, context);
            // init templateType data to datamode
            ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
            templateType.initTemplateData(site.getSiteId(), context);

            long s = System.currentTimeMillis();
            context.setDirectory(SiteUtils.getSiteRoot(site, publishPipeCode));
            context.setFirstFileName("index" + StringUtils.DOT + site.getStaticSuffix(publishPipeCode));
            this.staticizeService.process(context);
            logger.debug("[{}]首页模板解析：{}，耗时：{}ms", publishPipeCode, site.getName(), (System.currentTimeMillis() - s));
        } catch (Exception e) {
            logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}][{1}]站点首页解析失败：{2}",
                    publishPipeCode, site.getName(), e.getMessage())), e);
        }
    }
}
