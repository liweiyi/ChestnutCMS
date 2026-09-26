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

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.email.EmailMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.dto.UpdateMessageConfigReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class MessageConfigServiceImplTest {

    private RedisCache redisCache;

    private EmailMessageType emailMessageType;

    private MessageConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        redisCache = mock(RedisCache.class);
        emailMessageType = mock(EmailMessageType.class);
        Map<String, IMessageType<?>> messageTypes = Map.of(
                IMessageType.BEAN_PREFIX + EmailMessageType.TYPE, emailMessageType);
        service = spy(new MessageConfigServiceImpl(redisCache, messageTypes));
        service.setApplicationContext(mock(ApplicationContext.class));
    }

    @Test
    void shouldClearOriginalEmailSenderAfterUpdate() {
        CcMessageConfig config = emailConfig(1L, "old.smtp.example.com", 465, "old@example.com");
        UpdateMessageConfigReq request = updateRequest(1L,
                emailProps("new.smtp.example.com", 465, "new@example.com"));
        doReturn(config).when(service).getById(1L);
        doReturn(true).when(service).updateById(any(CcMessageConfig.class));

        service.updateConfig(request);

        verify(emailMessageType).clearSender("old.smtp.example.com", 465, "old@example.com");
    }

    @Test
    void shouldClearEmailSenderAfterDelete() {
        List<Long> configIds = List.of(1L);
        CcMessageConfig config = emailConfig(1L, "smtp.163.com", 465, "sender@163.com");
        doReturn(List.of(config)).when(service).listByIds(configIds);
        doReturn(true).when(service).removeByIds(configIds);

        service.deleteConfigs(configIds);

        verify(emailMessageType).clearSender("smtp.163.com", 465, "sender@163.com");
    }

    private CcMessageConfig emailConfig(Long configId, String host, int port, String user) {
        CcMessageConfig config = new CcMessageConfig();
        config.setConfigId(configId);
        config.setType(EmailMessageType.TYPE);
        config.setName("email config");
        config.setConfigProps(emailProps(host, port, user));
        return config;
    }

    private UpdateMessageConfigReq updateRequest(Long configId, ObjectNode props) {
        LoginUser operator = new LoginUser();
        operator.setUsername("tester");
        UpdateMessageConfigReq request = new UpdateMessageConfigReq();
        request.setConfigId(configId);
        request.setType(EmailMessageType.TYPE);
        request.setName("updated email config");
        request.setConfigProps(props);
        request.setOperator(operator);
        return request;
    }

    private ObjectNode emailProps(String host, int port, String user) {
        return JacksonUtils.objectNode()
                .put("host", host)
                .put("port", port)
                .put("secure", "Y")
                .put("user", user)
                .put("password", "authorization-code")
                .put("from", "ChestnutCMS <" + user + ">");
    }
}
