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
package com.chestnut.message.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.message.config.MessageFreeMarkerTemplateLoader;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.domain.dto.CreateMessageTemplateReq;
import com.chestnut.message.domain.dto.UpdateMessageTemplateReq;
import com.chestnut.message.mapper.CcMessageTemplateMapper;
import com.chestnut.message.monitor.MessageTemplateMonitoredCache;
import com.chestnut.message.service.IMessageTemplateService;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessageTemplateServiceImpl extends ServiceImpl<CcMessageTemplateMapper, CcMessageTemplate>
		implements IMessageTemplateService, CommandLineRunner {

    private final Configuration cfg;

    private final MessageFreeMarkerTemplateLoader freeMarkerTemplateLoader;

    private final MessageTemplateMonitoredCache monitoredCache;

    public MessageTemplateServiceImpl(
            @Qualifier("messageFreeMakerConfiguration") Configuration cfg,
            MessageTemplateMonitoredCache monitoredCache
    ) {
        this.cfg = cfg;
        this.freeMarkerTemplateLoader = (MessageFreeMarkerTemplateLoader) cfg.getTemplateLoader();
        this.monitoredCache = monitoredCache;
    }

    @Override
    public void addTemplate(CreateMessageTemplateReq req) {
        CcMessageTemplate template = new CcMessageTemplate();
        BeanUtils.copyProperties(req, template);
        template.setTemplateId(IdUtils.getSnowflakeId());
        template.createBy(req.getOperator().getUsername());
        this.save(template);
        freeMarkerTemplateLoader.addTemplate(template);
    }

    @Override
    public void updateTemplate(UpdateMessageTemplateReq req) {
        CcMessageTemplate dbTemplate = this.getById(req.getTemplateId());
        Assert.notNull(dbTemplate, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", req.getTemplateId()));

        BeanUtils.copyProperties(req, dbTemplate);
        dbTemplate.updateBy(req.getOperator().getUsername());
        this.updateById(dbTemplate);
        monitoredCache.deleteCache(dbTemplate.getTemplateId());
        freeMarkerTemplateLoader.addTemplate(dbTemplate);
    }

    @Override
    public void deleteTemplates(List<Long> ids) {
        List<CcMessageTemplate> messageTemplates = this.listByIds(ids);
        this.removeByIds(ids);
        ids.forEach(monitoredCache::deleteCache);
        freeMarkerTemplateLoader.removeTemplate(messageTemplates);
    }

    @Override
    public String parseTemplateTitle(CcMessageTemplate template, ObjectNode variables) throws TemplateException, IOException {
        return parseTemplate("title#" + template.getTemplateId(), variables);
    }

    @Override
    public String parseTemplateContent(CcMessageTemplate template, ObjectNode variables) throws TemplateException, IOException {
        return parseTemplate("content#" + template.getTemplateId(), variables);
    }

    @Override
    public String parseTemplate(String templateId, ObjectNode variables) throws IOException, TemplateException {
        Template template = cfg.getTemplate(templateId);

        long s = System.currentTimeMillis();
        StringWriter writer = new StringWriter();
        Environment env = template.createProcessingEnvironment(variables, writer);
        for (Map.Entry<String, JsonNode> entry : variables.properties()) {
            env.setGlobalVariable(entry.getKey(), env.getObjectWrapper().wrap(entry.getValue().toString()));
        }
        env.process();
        log.debug("Message template '{}' processed, cost: {}ms", templateId, System.currentTimeMillis() - s);
        return writer.toString();
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        long s = System.currentTimeMillis();
        List<CcMessageTemplate> list = this.lambdaQuery().list();
        for (CcMessageTemplate template : list) {
            freeMarkerTemplateLoader.addTemplate(template);
        }
        log.info("Load message template to freemarker cost: {}ms", System.currentTimeMillis() - s);
    }
}
