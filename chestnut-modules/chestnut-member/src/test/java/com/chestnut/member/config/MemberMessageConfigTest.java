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
package com.chestnut.member.config;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.message.core.email.EmailMessageType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberMessageConfigTest {

    private final List<IMemberConfig<Long[]>> configs = List.of(
            new MemberConfig_RegisterEmailMessage(),
            new MemberConfig_AccountEmailMessage()
    );

    @Test
    void shouldUseSingleArrayValueForMessageConfigAndTemplate() {
        ObjectNode values = JacksonUtils.objectNode();

        for (IMemberConfig<Long[]> config : configs) {
            assertArrayEquals(new Long[]{0L, 0L}, config.getValue(values));
            assertTrue(config.validate(new Long[]{1L, 10L}));
            assertTrue(EmailMessageType.TYPE.equals(config.getMessageType()));
            assertTrue(config.getControlType() == MemberConfigControlType.MESSAGE);
        }
    }

    @Test
    void shouldReadMessageConfigArray() {
        IMemberConfig<Long[]> config = new MemberConfig_RegisterEmailMessage();
        ObjectNode values = JacksonUtils.objectNode();
        values.set(config.getId(), JacksonUtils.arrayNode().add(1).add(10));

        assertArrayEquals(new Long[]{1L, 10L}, config.getValue(values));
    }

    @Test
    void shouldConvertLegacySplitValues() {
        MemberConfig_RegisterEmailMessage config = new MemberConfig_RegisterEmailMessage();
        ObjectNode values = JacksonUtils.objectNode()
                .put("registerEmailConfigId", "1234567890123456789")
                .put("registerEmailTemplateId", "10");

        assertArrayEquals(new Long[]{1234567890123456789L, 10L}, config.getValue(values));
        assertTrue(config.getDeprecatedIds().contains("registerEmailConfigId"));
        assertTrue(config.getDeprecatedIds().contains("registerEmailTemplateId"));
    }

    @Test
    void shouldRejectInvalidMessageArray() {
        for (IMemberConfig<Long[]> config : configs) {
            assertFalse(config.validate(null));
            assertFalse(config.validate(new Long[]{1L}));
            assertFalse(config.validate(new Long[]{-1L, 10L}));
            assertFalse(config.validate(new Long[]{1L, null}));
        }
    }
}
