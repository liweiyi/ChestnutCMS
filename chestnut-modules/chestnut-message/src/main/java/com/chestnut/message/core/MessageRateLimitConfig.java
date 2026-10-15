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
package com.chestnut.message.core;

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.message.exception.MessageErrorCode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有消息类型共用的发送频控配置，保存在消息配置的 configProps.rateLimits 中。
 * 缺失、null 或空对象表示没有频控窗口。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public record MessageRateLimitConfig(List<RateLimitWindow> windows) {

    public static final String RATE_LIMITS_PROP = "rateLimits";

    public MessageRateLimitConfig {
        windows = List.copyOf(windows);
    }

    public static MessageRateLimitConfig fromJson(JsonNode json) {
        if (json == null || json.isNull()) {
            return new MessageRateLimitConfig(List.of());
        }
        Assert.isTrue(json.isObject(), MessageErrorCode.INVALID_RATE_LIMIT_CONFIG::exception);
        return new MessageRateLimitConfig(readWindows(json.get(RATE_LIMITS_PROP)));
    }

    public static void normalize(ObjectNode json) {
        if (json == null) {
            return;
        }
        MessageRateLimitConfig config = fromJson(json);
        if (config.windows().isEmpty()) {
            return;
        }
        ObjectNode rateLimits = JacksonUtils.objectNode();
        config.windows().forEach(window -> rateLimits.put(
                String.valueOf(window.windowSeconds()),
                String.valueOf(window.maxCount())
        ));
        json.set(RATE_LIMITS_PROP, rateLimits);
    }

    public Map<String, String> toMap() {
        Map<String, String> rateLimits = new LinkedHashMap<>();
        this.windows.forEach(window -> rateLimits.put(
                String.valueOf(window.windowSeconds()),
                String.valueOf(window.maxCount())
        ));
        return rateLimits;
    }

    private static List<RateLimitWindow> readWindows(JsonNode rateLimits) {
        if (rateLimits == null || rateLimits.isNull()) {
            return List.of();
        }
        Assert.isTrue(rateLimits.isObject(), MessageErrorCode.INVALID_RATE_LIMIT_CONFIG::exception);
        List<RateLimitWindow> windows = new ArrayList<>();
        rateLimits.properties().forEach(entry -> windows.add(new RateLimitWindow(
                parsePositiveInt(entry.getKey()),
                parsePositiveInt(entry.getValue().asString())
        )));
        windows.sort((left, right) -> Integer.compare(left.windowSeconds(), right.windowSeconds()));
        for (int i = 1; i < windows.size(); i++) {
            Assert.isTrue(windows.get(i - 1).windowSeconds() != windows.get(i).windowSeconds(),
                    MessageErrorCode.INVALID_RATE_LIMIT_CONFIG::exception);
        }
        return windows;
    }

    private static int parsePositiveInt(String value) {
        Assert.notNull(value, MessageErrorCode.INVALID_RATE_LIMIT_CONFIG::exception);
        try {
            int parsed = Integer.parseInt(value.trim());
            Assert.isTrue(parsed > 0, MessageErrorCode.INVALID_RATE_LIMIT_CONFIG::exception);
            return parsed;
        } catch (NumberFormatException e) {
            throw MessageErrorCode.INVALID_RATE_LIMIT_CONFIG.exception(e);
        }
    }

    public record RateLimitWindow(int windowSeconds, int maxCount) {

        public RateLimitWindow {
            Assert.isTrue(windowSeconds > 0 && maxCount > 0,
                    MessageErrorCode.INVALID_RATE_LIMIT_CONFIG::exception);
        }
    }
}
