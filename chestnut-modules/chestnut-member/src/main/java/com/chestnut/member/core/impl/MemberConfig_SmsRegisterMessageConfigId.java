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

import com.chestnut.member.core.IMemberConfig;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * 会员短信登录消息配置ID。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberConfig.BEAN_PREFIX + MemberConfig_SmsRegisterMessageConfigId.ID)
public class MemberConfig_SmsRegisterMessageConfigId implements IMemberConfig<Long> {

    public static final String ID = "smsRegisterMessageConfigId";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{MEMBER.CONFIG.SMS_REGISTER_MESSAGE_CONFIG_ID}";
    }

    @Override
    public Class<Long> getValueType() {
        return Long.class;
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }

    @Override
    public int getOrder() {
        return 16;
    }

    @Override
    public Long getValue(ObjectNode configs) {
        if (Objects.nonNull(configs)) {
            return configs.path(ID).asLong(this.getDefaultValue());
        }
        return this.getDefaultValue();
    }

    @Override
    public boolean validate(Long value) {
        return value >= 0;
    }
}
