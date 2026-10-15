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
package com.chestnut.member.core.impl;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.member.core.IMemberConfig;
import com.chestnut.member.core.MemberConfigControlType;
import com.chestnut.message.core.email.EmailMessageType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * 会员注册邮件验证码消息配置。
 *
 * <p>数组第一个元素为消息配置 ID，第二个元素为消息模板 ID。</p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberConfig.BEAN_PREFIX + MemberConfig_RegisterEmailMessage.ID)
public class MemberConfig_RegisterEmailMessage implements IMemberConfig<Long[]> {

    public static final String ID = "registerEmailMessage";

    public static final int CONFIG_ID_INDEX = 0;

    public static final int TEMPLATE_ID_INDEX = 1;

    private static final String LEGACY_CONFIG_ID = "registerEmailConfigId";

    private static final String LEGACY_TEMPLATE_ID = "registerEmailTemplateId";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{MEMBER.CONFIG.REGISTER_EMAIL_MESSAGE}";
    }

    @Override
    public Class<Long[]> getValueType() {
        return Long[].class;
    }

    @Override
    public Long[] getDefaultValue() {
        return new Long[]{0L, 0L};
    }

    @Override
    public MemberConfigControlType getControlType() {
        return MemberConfigControlType.MESSAGE;
    }

    @Override
    public String getMessageType() {
        return EmailMessageType.TYPE;
    }

    @Override
    public List<String> getDeprecatedIds() {
        return List.of(LEGACY_CONFIG_ID, LEGACY_TEMPLATE_ID);
    }

    @Override
    public int getOrder() {
        return 21;
    }

    @Override
    public Long[] getValue(ObjectNode configs) {
        if (configs == null || configs.get(ID) == null || configs.get(ID).isNull()) {
            return new Long[]{readLegacyId(configs, LEGACY_CONFIG_ID), readLegacyId(configs, LEGACY_TEMPLATE_ID)};
        }
        return IMemberConfig.super.getValue(configs);
    }

    @Override
    public boolean validate(Long[] value) {
        return value != null && value.length == 2
                && value[CONFIG_ID_INDEX] != null && value[CONFIG_ID_INDEX] >= 0
                && value[TEMPLATE_ID_INDEX] != null && value[TEMPLATE_ID_INDEX] >= 0;
    }

    private Long readLegacyId(ObjectNode configs, String id) {
        JsonNode value = configs == null ? null : configs.get(id);
        return value == null || value.isNull() ? 0L : JacksonUtils.convertValue(value, Long.class);
    }

    public record MemberEmailRegisterMessageConfig(Long messageConfigId, Long messageTemplateConfigId) {

        public static MemberEmailRegisterMessageConfig decode(Long[] configIds) {
            Assert.isTrue(configIds.length == 2, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("configIds"));
            return new MemberEmailRegisterMessageConfig(configIds[CONFIG_ID_INDEX], configIds[TEMPLATE_ID_INDEX]);
        }
    }
}
