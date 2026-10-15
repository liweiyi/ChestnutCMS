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
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.MessageRateLimitConfig;
import com.chestnut.message.core.MessageRateLimitConfig.RateLimitWindow;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.CcMessageTemplate;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.message.monitor.MessageTemplateMonitoredCache;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.message.service.IMessageTemplateService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * 所有消息发送入口共用的投递服务，在调用渠道适配器之前执行频控。
 */
@Service
@RequiredArgsConstructor
public class MessageDeliveryService {

    private static final String RATE_LIMIT_KEY_PREFIX = "cc:message:rate:";

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

    private final RedisTemplate<String, Object> redisTemplate;

    private final IMessageConfigService messageConfigService;

    private final IMessageTemplateService messageTemplateService;

    private final MessageTemplateMonitoredCache messageTemplateCache;

    /**
     * 发起消息发送
     */
    public void sendMessage(IMessageType.SendMessageReq req) {
        Assert.notNull(req, MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);
        Assert.isTrue(IdUtils.validate(req.getMessageConfigId()), MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);
        // 获取消息配置信息
        CcMessageConfig config = messageConfigService.getMessageConfig(req.getMessageConfigId());
        Assert.notNull(config, () -> MessageErrorCode.MESSAGE_CONFIG_NOT_FOUND.exception(req.getMessageConfigId()));
        // 获取消息类型
        IMessageType<?> mt = this.messageConfigService.getMessageType(config.getType());
        Assert.notNull(mt, () -> MessageErrorCode.UNSUPPORTED_MESSAGE_TYPE.exception(config.getType()));
        // 校验发送参数
        mt.checkSendRequest(req);
        // 读取消息模版
        CcMessageTemplate template = null;
        if (mt.supportedTemplate()) {
            template = messageTemplateCache.get(req.getMessageTemplateId());
            Assert.notNull(template, () -> MessageErrorCode.MESSAGE_TEMPLATE_NOT_FOUND.exception(req.getMessageTemplateId()));
            Assert.isTrue(config.getType().equals(template.getType()), MessageErrorCode.MESSAGE_TYPE_MISMATCH::exception);
        }
        // 消息自定义参数
        ObjectNode variables = Objects.requireNonNullElseGet(req.getTemplateVariables(), JacksonUtils::objectNode);
        // 构建通用消息发送对象
        IMessageType.Message message = buildMessage(mt, req.getReceiver(), config, template, variables);
        String rateLimitKeyPrefix = mt.getType() + ":" + config.getConfigId();
        MessageRateLimitConfig rateLimits = MessageRateLimitConfig.fromJson(config.getConfigProps());
        // 异步频控发送
        if (!rateLimits.windows().isEmpty()) {
            String rateLimitKey = StringUtils.appendIfNotEmpty(rateLimitKeyPrefix, message.receiver(), v -> ":" + sha256(v));
            checkRateLimit(rateLimitKey, rateLimits);
        }
        // 发送
        mt.send(message);
    }

    public void testSendMessage(CcMessageConfig config, String receiver, String title, String content, ObjectNode templateVariables) {
        // 测试发送
        IMessageType<?> mt = this.messageConfigService.getMessageType(config.getType());
        ObjectNode messageProps = config.getConfigProps().deepCopy();
        messageProps.set("templateVariables", Objects.requireNonNullElse(templateVariables, JacksonUtils.objectNode()));
        IMessageType.Message message = new IMessageType.Message(title, content, receiver, messageProps);
        mt.send(message);
    }

    private IMessageType.Message buildMessage(IMessageType<?> messageType, String receiver, CcMessageConfig config,
                                              CcMessageTemplate template, ObjectNode templateVariables) {
        try {
            ObjectNode messageProps = config.getConfigProps().deepCopy();
            messageProps.set("templateVariables", templateVariables);
            String title = messageType.supportedTemplate() ? parseTitleVariables(template, templateVariables) : null;
            String content = messageType.supportedTemplate() ? parseContentVariables(template, templateVariables) : null;
            return new IMessageType.Message(title, content, receiver, messageProps);
        } catch (IOException | TemplateException | RuntimeException e) {
            if (e instanceof GlobalException globalException) {
                throw globalException;
            }
            throw MessageErrorCode.SEND_MESSAGE_FAIL.exception(e, e.getMessage());
        }
    }

    private String parseTitleVariables(CcMessageTemplate template, ObjectNode variables) throws TemplateException, IOException {
        if (Objects.isNull(template) || StringUtils.isBlank(template.getTitle())) {
            return null;
        }
        return messageTemplateService.parseTemplateTitle(template, variables);
    }

    private String parseContentVariables(CcMessageTemplate template, ObjectNode variables) throws TemplateException, IOException {
        if (Objects.isNull(template) || StringUtils.isBlank(template.getContent())) {
            return null;
        }
        return messageTemplateService.parseTemplateContent(template, variables);
    }

    private void checkRateLimit(String keyPart, MessageRateLimitConfig config) {
        // 机器人按配置计数；短信、邮件在配置内按接收人计数。
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
