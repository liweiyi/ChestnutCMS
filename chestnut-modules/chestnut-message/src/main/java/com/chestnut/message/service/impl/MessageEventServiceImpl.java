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
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.message.exception.MessageTips;
import com.chestnut.message.mapper.CcMessageEventMapper;
import com.chestnut.message.monitor.MessageTemplateMonitoredCache;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.message.service.IMessageEventService;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageEventServiceImpl extends ServiceImpl<CcMessageEventMapper, CcMessageEvent>
		implements IMessageEventService {

    private final RedissonClient redissonClient;

    private final IMessageConfigService messageConfigService;

    private final MessageTemplateMonitoredCache templateMonitoredCache;

    private final MessageDeliveryService messageDeliveryService;

    @Override
    public void saveEvent(SaveMessageEventReq req) {
        for (CcMessageEvent.EventNotify notify : req.getNotifies()) {
            CcMessageConfig config = messageConfigService.getMessageConfig(notify.getConfigId());
            Assert.notNull(config, () -> MessageErrorCode.MESSAGE_CONFIG_NOT_FOUND.exception(notify.getConfigId()));
            IMessageType<?> type = messageConfigService.getMessageType(config.getType());
            if (type.requiresReceiver()) {
                List<String> receivers = List.of();
                if (Objects.nonNull(notify.getReceivers())) {
                    receivers = notify.getReceivers().stream().filter(StringUtils::isNotBlank).distinct().toList();
                }
                Assert.isTrue(StringUtils.isNotEmpty(receivers), MessageErrorCode.INVALID_MESSAGE_RECEIVER::exception);
                notify.setReceivers(receivers);
            } else {
                notify.setReceivers(List.of());
            }
            if (type.supportedTemplate()) {
                CcMessageTemplate template = templateMonitoredCache.get(notify.getTemplateId());
                Assert.notNull(template, () -> MessageErrorCode.MESSAGE_TEMPLATE_NOT_FOUND.exception(notify.getTemplateId()));
                Assert.isTrue(config.getType().equals(template.getType()), MessageErrorCode.MESSAGE_TYPE_MISMATCH::exception);
            }
            notify.setType(config.getType());
            notify.setName(config.getName());
        }
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
        if (EnableOrDisable.isDisable(event.getStatus())) {
            return;
        }
        for (int i = 0; i < event.getNotifies().size(); i++) {
            CcMessageEvent.EventNotify notify = event.getNotifies().get(i);
            AsyncTaskManager.setTaskProgressInfo((i + 1) * 100 / event.getNotifies().size(), MessageTips.SENDING_MESSAGE,
                    notify.getType(), notify.getName(), notify.getConfigId());
            try {
                CcMessageConfig messageConfig = this.messageConfigService.getMessageConfig(notify.getConfigId());
                IMessageType<?> mt = this.messageConfigService.getMessageType(messageConfig.getType());
                if (mt.requiresReceiver()) {
                    // 不再兼容历史，一律从receivers中读取接收人
                    for (String receiver : notify.getReceivers()) {
                        this.asyncSendMessage(eventId, notify, receiver, templateVariables);
                    }
                } else {
                    this.asyncSendMessage(eventId, notify, null, templateVariables);
                }
            } catch (Exception e) {
                this.reportSendFailure(eventId, notify, e);
            }
        }
    }

    private void asyncSendMessage(String eventId, CcMessageEvent.EventNotify notify, String receiver, ObjectNode variables) {
        try {
            this.messageDeliveryService.sendMessage(new IMessageType.SendMessageReq(
                    receiver,
                    notify.getConfigId(),
                    notify.getTemplateId(),
                    variables
            ));
        } catch (Exception e) {
            reportSendFailure(eventId, notify, e);
        }
    }

    private void reportSendFailure(String eventId, CcMessageEvent.EventNotify notify, Exception exception) {
        AsyncTaskManager.addErrMessage("发送消息失败：configId=%s,type=%s,name=%s"
                .formatted(notify.getConfigId(), notify.getType(), notify.getName()));
        log.error("Send event message fail: eventId={}, configId={}", eventId, notify.getConfigId(), exception);
    }
}
