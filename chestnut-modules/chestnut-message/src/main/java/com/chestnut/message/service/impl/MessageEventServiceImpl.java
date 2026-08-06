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
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.CcMessageEvent;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.domain.dto.SaveMessageEventReq;
import com.chestnut.message.exception.MessageTips;
import com.chestnut.message.mapper.CcMessageEventMapper;
import com.chestnut.message.monitor.MessageTemplateMonitoredCache;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.message.service.IMessageEventService;
import com.chestnut.message.service.IMessageTemplateService;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageEventServiceImpl extends ServiceImpl<CcMessageEventMapper, CcMessageEvent>
		implements IMessageEventService {

    private final RedissonClient redissonClient;

    private final IMessageConfigService messageConfigService;

    private final IMessageTemplateService messageTemplateService;

    private final MessageTemplateMonitoredCache templateMonitoredCache;

    @Override
    public void saveEvent(SaveMessageEventReq req) {
        RLock lock = redissonClient.getLock("MessageEvent-" + req.getEventId());
        lock.lock();
        try {
            CcMessageEvent event = this.getById(req.getEventId());
            if (Objects.isNull(event)) {
                event = new CcMessageEvent();
            }
            BeanUtils.copyProperties(req, event);
            if (Objects.isNull(event.getCreateTime())) {
                event.createBy(req.getOperator().getUsername());
                this.save(event);
            } else {
                event.updateBy(req.getOperator().getUsername());
                this.updateById(event);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteEvents(List<String> ids) {
        this.removeByIds(ids);
    }

    @Override
    public void triggerEvent(String eventId, ObjectNode templateVariables) {
        CcMessageEvent event = this.getById(eventId);
        Assert.notNull(event, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("eventId", eventId));

        for (int i = 0; i < event.getNotifies().size(); i++) {
            CcMessageEvent.EventNotify notify = event.getNotifies().get(i);
            CcMessageTemplate template = templateMonitoredCache.get(notify.getTemplateId());
            if (Objects.isNull(template)) {
                return;
            }
            AsyncTaskManager.setTaskProgressInfo((i + 1) * 100 / event.getNotifies().size(), MessageTips.SENDING_MESSAGE,
                    notify.getConfigId(), notify.getType(), notify.getName());
            try {
                String title = template.getTitle();
                String content = template.getContent();
                if (StringUtils.isNotEmpty(template.getTitle())) {
                    title = messageTemplateService.parseTemplateTitle(template, templateVariables);
                }
                if (StringUtils.isNotEmpty(template.getContent())) {
                    content = messageTemplateService.parseTemplateContent(template, templateVariables);
                }
                CcMessageConfig messageConfig = messageConfigService.getMessageConfig(notify.getConfigId());
                IMessageType<?> messageType = messageConfigService.getMessageType(messageConfig.getType());
                ObjectNode jsonSendParams = notify.getParams();
                jsonSendParams.set("templateVariables", templateVariables);
                IMessageType.Message message = new IMessageType.Message(title, content, jsonSendParams);
                messageType.send(messageConfig.getConfigProps(), message);
            } catch (Exception e) {
                AsyncTaskManager.addErrMessage("发送消息失败：configId=%s,type=%s,name=%s"
                        .formatted(notify.getConfigId(), notify.getType(), notify.getName()));
                log.error("Send message fail: {}", e.getMessage());
            }
        }
    }
}
