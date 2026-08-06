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
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.MessageRateLimitConfig;
import com.chestnut.message.core.MessageRateLimitConfig.RateLimitWindow;
import com.chestnut.message.core.email.EmailMessageType;
import com.chestnut.message.core.sms.SmsMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.domain.dto.UserMessageSendRequest;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.message.monitor.MessageTemplateMonitoredCache;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.message.service.IMessageSendService;
import com.chestnut.message.service.IMessageTemplateService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MessageSendServiceImpl implements IMessageSendService {

    private static final String RATE_LIMIT_KEY_PREFIX = "cc:message:rate:";

    private static final Pattern BUSINESS_SCENE_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,100}");

    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?[0-9]{6,20}");

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>("""
            for index, key in ipairs(KEYS) do
                local maxCount = tonumber(ARGV[(index - 1) * 2 + 1])
                local current = tonumber(redis.call('get', key) or '0')
                if current >= maxCount then
                    return index
                end
            end
            for index, key in ipairs(KEYS) do
                local windowSeconds = tonumber(ARGV[(index - 1) * 2 + 2])
                local current = redis.call('incr', key)
                if current == 1 then
                    redis.call('expire', key, windowSeconds)
                end
            end
            return 0
            """, Long.class);

    private final IMessageConfigService messageConfigService;

    private final IMessageTemplateService messageTemplateService;

    private final MessageTemplateMonitoredCache messageTemplateCache;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void sendUserMessage(UserMessageSendRequest req) {
        Assert.notNull(req, MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);
        String messageType = req.getMessageType();
        Assert.isTrue(EmailMessageType.TYPE.equals(messageType) || SmsMessageType.TYPE.equals(messageType),
                () -> MessageErrorCode.UNSUPPORTED_USER_MESSAGE_TYPE.exception(messageType));
        Assert.isTrue(StringUtils.isNotEmpty(req.getBusinessScene())
                        && BUSINESS_SCENE_PATTERN.matcher(req.getBusinessScene()).matches(),
                MessageErrorCode.INVALID_BUSINESS_SCENE::exception);
        Assert.isTrue(Objects.nonNull(req.getConfigId()) && req.getConfigId() > 0
                        && Objects.nonNull(req.getTemplateId()) && req.getTemplateId() > 0,
                MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);

        String receiver = normalizeReceiver(messageType, req.getUserIdentifier());
        CcMessageConfig config = messageConfigService.getMessageConfig(req.getConfigId());
        Assert.notNull(config, () -> MessageErrorCode.MESSAGE_CONFIG_NOT_FOUND.exception(req.getConfigId()));
        CcMessageTemplate template = messageTemplateCache.get(req.getTemplateId());
        Assert.notNull(template, () -> MessageErrorCode.MESSAGE_TEMPLATE_NOT_FOUND.exception(req.getTemplateId()));
        Assert.isTrue(messageType.equals(config.getType()) && messageType.equals(template.getType()),
                MessageErrorCode.MESSAGE_TYPE_MISMATCH::exception);

        ObjectNode variables = Objects.requireNonNullElseGet(req.getParams(), JacksonUtils::objectNode);
        IMessageType<?> type = messageConfigService.getMessageType(messageType);
        IMessageType.Message message = buildMessage(messageType, receiver, config, template, variables);
        MessageRateLimitConfig rateLimitConfig = MessageRateLimitConfig.fromJson(config.getConfigProps());
        checkRateLimit(messageType, req.getBusinessScene(), receiver, rateLimitConfig);
        type.send(config.getConfigProps(), message);
    }

    private IMessageType.Message buildMessage(String messageType, String receiver, CcMessageConfig config,
                                               CcMessageTemplate template, ObjectNode variables) {
        try {
            String title = StringUtils.isEmpty(template.getTitle()) ? ""
                    : messageTemplateService.parseTemplateTitle(template, variables);
            String content = StringUtils.isEmpty(template.getContent()) ? ""
                    : messageTemplateService.parseTemplateContent(template, variables);
            ObjectNode sendParams = JacksonUtils.objectNode();
            ArrayNode receivers = JacksonUtils.arrayNode().add(receiver);
            if (EmailMessageType.TYPE.equals(messageType)) {
                sendParams.set("mails", receivers);
            } else {
                sendParams.set("phoneNumbers", receivers);
                sendParams.put("templateId", config.getConfigProps().required("templateId").asString());
                sendParams.set("templateVariables", variables);
            }
            return new IMessageType.Message(title, content, sendParams);
        } catch (IOException | TemplateException | RuntimeException e) {
            if (e instanceof GlobalException globalException) {
                throw globalException;
            }
            throw MessageErrorCode.SEND_MESSAGE_FAIL.exception(e, e.getMessage());
        }
    }

    private void checkRateLimit(String messageType, String businessScene, String receiver,
                                MessageRateLimitConfig config) {
        String keyPart = messageType + ":" + businessScene + ":" + sha256(receiver);
        String redisKeyPrefix = RATE_LIMIT_KEY_PREFIX + "{" + keyPart + "}:";
        List<RateLimitWindow> windows = config.windows();
        List<String> redisKeys = new ArrayList<>(windows.size());
        Object[] args = new Object[windows.size() * 2];
        for (int i = 0; i < windows.size(); i++) {
            RateLimitWindow window = windows.get(i);
            redisKeys.add(redisKeyPrefix + window.windowSeconds());
            args[i * 2] = window.maxCount();
            args[i * 2 + 1] = window.windowSeconds();
        }
        try {
            Long rejectedWindowIndex = redisTemplate.execute(RATE_LIMIT_SCRIPT, redisKeys, args);
            if (rejectedWindowIndex == null) {
                throw MessageErrorCode.MESSAGE_RATE_LIMIT_ERROR.exception();
            }
            if (rejectedWindowIndex > 0) {
                RateLimitWindow rejectedWindow = windows.get(rejectedWindowIndex.intValue() - 1);
                throw MessageErrorCode.MESSAGE_RATE_LIMIT.exception(
                        rejectedWindow.windowSeconds(), rejectedWindow.maxCount());
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw MessageErrorCode.MESSAGE_RATE_LIMIT_ERROR.exception(e);
        }
    }

    private String normalizeReceiver(String messageType, String userIdentifier) {
        Assert.notEmpty(userIdentifier, MessageErrorCode.INVALID_MESSAGE_RECEIVER::exception);
        if (EmailMessageType.TYPE.equals(messageType)) {
            String email = userIdentifier.trim().toLowerCase(Locale.ROOT);
            int at = email.lastIndexOf('@');
            Assert.isTrue(at > 0 && at < email.length() - 1,
                    MessageErrorCode.INVALID_MESSAGE_RECEIVER::exception);
            return email;
        }
        String phone = userIdentifier.replaceAll("[\\s-]", "");
        Assert.isTrue(PHONE_PATTERN.matcher(phone).matches(),
                MessageErrorCode.INVALID_MESSAGE_RECEIVER::exception);
        return phone;
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw MessageErrorCode.MESSAGE_RATE_LIMIT_ERROR.exception(e);
        }
    }
}
