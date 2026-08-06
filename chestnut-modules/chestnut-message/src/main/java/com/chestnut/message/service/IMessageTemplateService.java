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
package com.chestnut.message.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.domain.dto.CreateMessageTemplateReq;
import com.chestnut.message.domain.dto.UpdateMessageTemplateReq;
import tools.jackson.databind.node.ObjectNode;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;

/**
 * 消息模板服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMessageTemplateService extends IService<CcMessageTemplate> {

    void addTemplate(CreateMessageTemplateReq req);

    void updateTemplate(UpdateMessageTemplateReq req);

    void deleteTemplates(List<Long> ids);

    String parseTemplateTitle(CcMessageTemplate template, ObjectNode variables) throws TemplateException, IOException;

    String parseTemplateContent(CcMessageTemplate template, ObjectNode variables) throws TemplateException, IOException;

    String parseTemplate(String templateId, ObjectNode variables) throws IOException, TemplateException;
}
