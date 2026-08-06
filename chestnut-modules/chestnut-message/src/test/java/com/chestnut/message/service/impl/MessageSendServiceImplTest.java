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

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.email.EmailMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.domain.dto.UserMessageSendRequest;
import com.chestnut.message.monitor.MessageTemplateMonitoredCache;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.message.service.IMessageTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSendServiceImplTest {

    @Mock
    private IMessageConfigService messageConfigService;

    @Mock
    private IMessageTemplateService messageTemplateService;

    @Mock
    private MessageTemplateMonitoredCache messageTemplateCache;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private IMessageType<?> messageType;

    private MessageSendServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessageSendServiceImpl(messageConfigService, messageTemplateService,
                messageTemplateCache, redisTemplate);
        CcMessageConfig config = new CcMessageConfig();
        config.setConfigId(1L);
        config.setType(EmailMessageType.TYPE);
        ObjectNode configProps = JacksonUtils.objectNode();
        configProps.set("rateLimits", JacksonUtils.objectNode()
                .put("60", "1")
                .put("3600", "5")
                .put("86400", "10"));
        config.setConfigProps(configProps);
        CcMessageTemplate template = new CcMessageTemplate();
        template.setTemplateId(2L);
        template.setType(EmailMessageType.TYPE);
        template.setTitle("");
        template.setContent("");
        when(messageConfigService.getMessageConfig(1L)).thenReturn(config);
        when(messageTemplateCache.get(2L)).thenReturn(template);
        doReturn(messageType).when(messageConfigService).getMessageType(EmailMessageType.TYPE);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldNormalizeReceiverAndBuildRateLimitKeys() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(),
                any(), any(), any(), any(), any(), any())).thenReturn(0L);

        service.sendUserMessage(request(" User@Example.COM "));

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).execute(any(RedisScript.class), keysCaptor.capture(),
                eq(1), eq(60), eq(5), eq(3600), eq(10), eq(86400));
        assertEquals(List.of(
                "cc:message:rate:{Email:member.register.email-code:b4c9a289323b21a01c3e940f150eb9b8c542587f1abfd8f0e1cc1ffc5e475514}:60",
                "cc:message:rate:{Email:member.register.email-code:b4c9a289323b21a01c3e940f150eb9b8c542587f1abfd8f0e1cc1ffc5e475514}:3600",
                "cc:message:rate:{Email:member.register.email-code:b4c9a289323b21a01c3e940f150eb9b8c542587f1abfd8f0e1cc1ffc5e475514}:86400"
        ), keysCaptor.getValue());

        ArgumentCaptor<IMessageType.Message> messageCaptor = ArgumentCaptor.forClass(IMessageType.Message.class);
        verify(messageType).send(any(ObjectNode.class), messageCaptor.capture());
        assertEquals("user@example.com", messageCaptor.getValue().params().get("mails").get(0).asString());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldNotSendWhenRateLimitIsReached() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(),
                any(), any(), any(), any(), any(), any())).thenReturn(2L);

        assertThrows(GlobalException.class, () -> service.sendUserMessage(request("user@example.com")));

        verify(messageType, never()).send(any(ObjectNode.class), any(IMessageType.Message.class));
    }

    private UserMessageSendRequest request(String receiver) {
        return new UserMessageSendRequest(
                EmailMessageType.TYPE,
                "member.register.email-code",
                receiver,
                1L,
                2L,
                JacksonUtils.objectNode().put("code", "123456")
        );
    }
}
