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
package com.chestnut.message.config;

import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.mapper.CcMessageTemplateMapper;
import freemarker.cache.TemplateLoader;
import lombok.Getter;
import lombok.Setter;

import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MessageFreeMarkerTemplateLoader
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class MessageFreeMarkerTemplateLoader implements TemplateLoader {

    private final ConcurrentHashMap<String, MessageTemplateSource> templates = new ConcurrentHashMap<>();

    private final CcMessageTemplateMapper messageTemplateMapper;

    public MessageFreeMarkerTemplateLoader(CcMessageTemplateMapper messageTemplateMapper) {
        this.messageTemplateMapper = messageTemplateMapper;
    }

    @Getter
    @Setter
    public static class MessageTemplateSource {
        private String templateId;
        private String templateContent;
        private long modified;

        public static List<MessageTemplateSource> of(CcMessageTemplate messageTemplate) {
            MessageTemplateSource titleTemplateSource = new MessageTemplateSource();
            titleTemplateSource.setTemplateId("title#" + messageTemplate.getTemplateId());
            titleTemplateSource.setTemplateContent(messageTemplate.getTitle());
            titleTemplateSource.setModified(Instant.now().toEpochMilli());

            MessageTemplateSource contentTemplateSource = new MessageTemplateSource();
            contentTemplateSource.setTemplateId("content#" + messageTemplate.getTemplateId());
            contentTemplateSource.setTemplateContent(messageTemplate.getContent());
            contentTemplateSource.setModified(Instant.now().toEpochMilli());

            return List.of(titleTemplateSource, contentTemplateSource);
        }
    }

    public void addTemplate(CcMessageTemplate messageTemplate) {
        List<MessageTemplateSource> messageTemplateSources = MessageTemplateSource.of(messageTemplate);
        messageTemplateSources.forEach(templateSource -> templates.put(templateSource.getTemplateId(), templateSource));
    }

    public void removeTemplate(CcMessageTemplate messageTemplate) {
        MessageTemplateSource remove1 = templates.remove("title#" + messageTemplate.getTemplateId());
        MessageTemplateSource remove2 = templates.remove("content#" + messageTemplate.getTemplateId());
        closeTemplateSource(remove1);
        closeTemplateSource(remove2);
    }

    public void removeTemplate(List<CcMessageTemplate> messageTemplates) {
        messageTemplates.forEach(this::removeTemplate);
    }

    @Override
    public Object findTemplateSource(String templateId) {
        MessageTemplateSource templateSource = templates.get(templateId);
        if (Objects.nonNull(templateSource)) {
            return templateSource;
        }
        String[] split = templateId.split("#");
        if (split.length != 2) {
            return null;
        }
        String messageTemplateId = split[1];
        CcMessageTemplate template = messageTemplateMapper.selectById(messageTemplateId);
        if (Objects.nonNull(template)) {
            addTemplate(template);
            return templates.get(templateId);
        }
        return null;
    }

    @Override
    public long getLastModified(Object o) {
        if (o instanceof MessageTemplateSource template) {
            return template.getModified();
        }
        return 0;
    }

    @Override
    public Reader getReader(Object o, String encoding) {
        if (o instanceof MessageTemplateSource template) {
            return new StringReader(template.getTemplateContent());
        }
        throw new RuntimeException("Failed to load message freemark template.");
    }

    @Override
    public void closeTemplateSource(Object o) {
    }
}
