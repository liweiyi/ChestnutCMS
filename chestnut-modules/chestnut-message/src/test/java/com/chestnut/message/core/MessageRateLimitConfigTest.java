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

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.JacksonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageRateLimitConfigTest {

    @Test
    void shouldUseDefaultsWhenRateLimitsAreMissing() {
        MessageRateLimitConfig config = MessageRateLimitConfig.fromJson(JacksonUtils.objectNode());

        assertEquals(List.of(new MessageRateLimitConfig.RateLimitWindow(60, 1)), config.windows());
    }

    @Test
    void shouldReadAndNormalizeMultipleWindows() {
        ObjectNode rateLimits = JacksonUtils.objectNode()
                .put("86400", "10")
                .put("60", "1")
                .put("3600", "5");
        ObjectNode json = JacksonUtils.objectNode().set(MessageRateLimitConfig.RATE_LIMITS_PROP, rateLimits);

        MessageRateLimitConfig.normalize(json);

        MessageRateLimitConfig config = MessageRateLimitConfig.fromJson(json);
        assertEquals(List.of(
                new MessageRateLimitConfig.RateLimitWindow(60, 1),
                new MessageRateLimitConfig.RateLimitWindow(3600, 5),
                new MessageRateLimitConfig.RateLimitWindow(86400, 10)
        ), config.windows());
        assertEquals(Map.of("60", "1", "3600", "5", "86400", "10"), config.toMap());
    }

    @Test
    void shouldConvertLegacySingleWindowConfig() {
        ObjectNode json = JacksonUtils.objectNode()
                .put(MessageRateLimitConfig.LEGACY_WINDOW_SECONDS_PROP, 300)
                .put(MessageRateLimitConfig.LEGACY_MAX_COUNT_PROP, 5);

        MessageRateLimitConfig.normalize(json);

        assertEquals("5", json.get(MessageRateLimitConfig.RATE_LIMITS_PROP).get("300").asString());
        assertFalse(json.has(MessageRateLimitConfig.LEGACY_WINDOW_SECONDS_PROP));
        assertFalse(json.has(MessageRateLimitConfig.LEGACY_MAX_COUNT_PROP));
    }

    @Test
    void shouldRejectEmptyOrNonPositiveWindows() {
        ObjectNode empty = JacksonUtils.objectNode()
                .set(MessageRateLimitConfig.RATE_LIMITS_PROP, JacksonUtils.objectNode());
        ObjectNode invalidWindow = JacksonUtils.objectNode()
                .set(MessageRateLimitConfig.RATE_LIMITS_PROP, JacksonUtils.objectNode().put("0", "1"));
        ObjectNode invalidCount = JacksonUtils.objectNode()
                .set(MessageRateLimitConfig.RATE_LIMITS_PROP, JacksonUtils.objectNode().put("60", "0"));
        ObjectNode duplicateWindow = JacksonUtils.objectNode()
                .set(MessageRateLimitConfig.RATE_LIMITS_PROP, JacksonUtils.objectNode()
                        .put("60", "1")
                        .put("060", "2"));

        assertThrows(GlobalException.class, () -> MessageRateLimitConfig.fromJson(empty));
        assertThrows(GlobalException.class, () -> MessageRateLimitConfig.fromJson(invalidWindow));
        assertThrows(GlobalException.class, () -> MessageRateLimitConfig.fromJson(invalidCount));
        assertThrows(GlobalException.class, () -> MessageRateLimitConfig.fromJson(duplicateWindow));
    }
}
