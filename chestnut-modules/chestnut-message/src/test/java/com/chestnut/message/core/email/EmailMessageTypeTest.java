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
package com.chestnut.message.core.email;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.message.core.IMessageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import tools.jackson.databind.node.ObjectNode;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EmailMessageTypeTest {

    @BeforeAll
    static void initializeYesOrNo() {
        new SpringUtils().postProcessBeanFactory(mock(ConfigurableListableBeanFactory.class));
    }

    @Test
    void shouldRebuildSenderAfterCachedSenderIsCleared() {
        AsyncTaskManager asyncTaskManager = mock(AsyncTaskManager.class);
        EmailMessageType messageType = spy(new EmailMessageType(asyncTaskManager));
        JavaMailSenderImpl firstSender = mock(JavaMailSenderImpl.class);
        JavaMailSenderImpl secondSender = mock(JavaMailSenderImpl.class);
        doReturn(firstSender, secondSender).when(messageType)
                .createJavaMailSender(any(EmailProps.class));

        messageType.send(emailConfig("N"), message());
        messageType.clearSender("smtp.163.com", 465, "sender@163.com");
        messageType.send(emailConfig("Y"), message());

        verify(messageType, times(2)).createJavaMailSender(any(EmailProps.class));
        verify(asyncTaskManager, times(2)).execute(any(Runnable.class));
    }

    @Test
    void shouldEnableImplicitSslWhenSecureIsYes() {
        EmailMessageType messageType = new EmailMessageType(mock(AsyncTaskManager.class));

        Properties insecureProperties = messageType.createJavaMailSender(
                new EmailProps().fromJson(emailConfig("N"))).getJavaMailProperties();
        assertNull(insecureProperties.getProperty("mail.smtp.ssl.enable"));

        Properties secureProperties = messageType.createJavaMailSender(
                new EmailProps().fromJson(emailConfig("Y"))).getJavaMailProperties();
        assertEquals("true", secureProperties.getProperty("mail.smtp.auth"));
        assertEquals("true", secureProperties.getProperty("mail.smtp.ssl.enable"));
        assertEquals("javax.net.ssl.SSLSocketFactory",
                secureProperties.getProperty("mail.smtp.socketFactory.class"));
        assertEquals("465", secureProperties.getProperty("mail.smtp.socketFactory.port"));
    }

    private ObjectNode emailConfig(String secure) {
        return JacksonUtils.objectNode()
                .put("host", "smtp.163.com")
                .put("port", 465)
                .put("secure", secure)
                .put("user", "sender@163.com")
                .put("password", "authorization-code")
                .put("from", "ChestnutCMS <sender@163.com>");
    }

    private IMessageType.Message message() {
        ObjectNode params = JacksonUtils.objectNode()
                .set("mails", JacksonUtils.arrayNode().add("receiver@example.com"));
        return new IMessageType.Message("test subject", "test content", params);
    }
}
