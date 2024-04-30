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
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.config.CMSPublishConfig;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.Objects;

/**
 * 站点发布任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IPublishTask.BeanPrefix + SitePublishTask.Type)
public class SitePublishTask implements IPublishTask<CmsSite> {

    public final static String Type = "site";

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final ITemplateService templateService;

    private final StaticizeService staticizeService;

    private final StringRedisTemplate redisTemplate;

    @Override
    public String getType() {
        return Type;
    }

    @Override
    public void publish(CmsSite site) {
        String dataId = site.getSiteId().toString();
        MapRecord<String, String, String> record = MapRecord.create(CMSPublishConfig.PublishStreamName, Map.of(
                "type", Type,
                "id", dataId
        ));
        redisTemplate.opsForStream().add(record);
    }

    @Override
    public void staticize(Map<String, String> dataMap) {
        logger.info("开始发布：" + JacksonUtils.to(dataMap));
        Long siteId = MapUtils.getLong(dataMap, "id");
        if (IdUtils.validate(siteId)) {
            CmsSite site = this.siteService.getSite(siteId);
            if (Objects.nonNull(site)) {
                this.siteStaticize(site);
            }
        }
        logger.info("结束发布：" + JacksonUtils.to(dataMap));
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
