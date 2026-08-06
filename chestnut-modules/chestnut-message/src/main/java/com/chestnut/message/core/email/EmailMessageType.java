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
import com.chestnut.common.utils.Assert;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.MessageRateLimitConfig;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.system.fixed.dict.YesOrNo;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件消息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IMessageType.BEAN_PREFIX + EmailMessageType.TYPE)
public class EmailMessageType implements IMessageType<EmailProps> {

    public static final String TYPE = "Email";

    public static final String NAME = "{MessageType." + TYPE + "}";

    private final AsyncTaskManager asyncTaskManager;

    private final ConcurrentHashMap<String, JavaMailSenderImpl> javaMailSenderMap = new ConcurrentHashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<EmailProps> getPropsClass() {
        return EmailProps.class;
    }

    @Override
    public void validate(ObjectNode configs) {
        MessageRateLimitConfig.normalize(configs);
    }

    @Override
    public void send(ObjectNode jsonConfigProps, Message message) {
        EmailProps props = new EmailProps().fromJson(jsonConfigProps);
        String senderKey = props.getHost() + ":" + props.getPort() + "#" + props.getUser();
        JavaMailSenderImpl javaMailSender = javaMailSenderMap.get(senderKey);
        if (Objects.isNull(javaMailSender)) {
            javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost(props.getHost());
            javaMailSender.setPort(props.getPort());
            javaMailSender.setUsername(props.getUser());
            javaMailSender.setPassword(props.getPassword());
            javaMailSender.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
            Properties properties = new Properties();
            if (YesOrNo.isYes(props.getSecure())) {
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.ssl.enable", "true");
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.put("mail.smtp.socketFactory.port", "465");
            }
            if (Objects.nonNull(props.getProperties())) {
                properties.putAll(props.getProperties());
            }
            javaMailSender.setJavaMailProperties(properties);
            javaMailSenderMap.put(senderKey, javaMailSender);
        }
        final JavaMailSenderImpl mailSender = javaMailSender;
        EmailSendParams emailSendParams = new EmailSendParams().fromJson(message.params());
        for (String mail : emailSendParams.getMails()) {
            // 异步发送，量多可以考虑通过MQ进行处理
            asyncTaskManager.execute(() -> {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(props.getFrom());
                mailMessage.setTo(mail);
                mailMessage.setSubject(message.title());
                mailMessage.setText(message.content());
                mailSender.send(mailMessage);
            });
        }
    }
}
