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

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.system.fixed.dict.YesOrNo;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
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

    private final ConcurrentHashMap<String, JavaMailSenderImpl> javaMailSenderMap = new ConcurrentHashMap<>();

    public static ObjectNode createSendParameters(String code) {
        return JacksonUtils.objectNode().put("code", code);
    }

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
    public boolean requiresReceiver() {
        return true;
    }

    @Override
    public void checkSendRequest(SendMessageReq req) {
        IMessageType.super.checkSendRequest(req);
        Assert.notEmpty(req.getReceiver(), MessageErrorCode.INVALID_MESSAGE_RECEIVER::exception);
        ValidationUtils.validateEmail(req.getReceiver(), MessageErrorCode.INVALID_MESSAGE_RECEIVER::exception);
        req.setReceiver(this.normalizeReceiver(req.getReceiver()));
    }

    private String normalizeReceiver(String receiver) {
        String[] split = receiver.split("@");
        return split[0] + "@" + split[1].toLowerCase(Locale.ROOT); // 邮箱域名统一转小写
    }

    @Override
    public void send(Message message) {
        EmailProps props = new EmailProps().fromJson(message.configProps());
        String senderKey = getSenderKey(props.getHost(), props.getPort(), props.getUser());
        JavaMailSenderImpl mailSender = javaMailSenderMap.computeIfAbsent(senderKey,
                key -> createJavaMailSender(props));
        try {
            MimeMessage mailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, StandardCharsets.UTF_8.name());
            helper.setFrom(props.getFrom());
            helper.setTo(message.receiver());
            helper.setSubject(message.title());
            helper.setText(message.content(), true);
            mailSender.send(mailMessage);
        } catch (Exception e) {
            throw MessageErrorCode.SEND_MESSAGE_FAIL.exception(e, e.getMessage());
        }
    }

    public void clearSender(String host, Integer port, String user) {
        javaMailSenderMap.remove(getSenderKey(host, port, user));
    }

    private String getSenderKey(String host, Integer port, String user) {
        return host + ":" + port + "#" + user;
    }

    JavaMailSenderImpl createJavaMailSender(EmailProps props) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
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
        return javaMailSender;
    }
}
