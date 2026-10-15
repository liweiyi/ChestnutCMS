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
import com.chestnut.member.core.MemberConfigControlType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * 会员登录验证码过期时间配置。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberConfig.BEAN_PREFIX + MemberConfig_LoginCaptchaExpireSeconds.ID)
public class MemberConfig_LoginCaptchaExpireSeconds implements IMemberConfig<Integer> {

    public static final String ID = "loginCaptchaExpireSeconds";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{MEMBER.CONFIG.LOGIN_CAPTCHA_EXPIRE_SECONDS}";
    }

    @Override
    public Class<Integer> getValueType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return 600;
    }

    @Override
    public MemberConfigControlType getControlType() {
        return MemberConfigControlType.INPUT_NUMBER;
    }

    @Override
    public int getOrder() {
        return 12;
    }

    @Override
    public Integer getValue(ObjectNode configs) {
        if (Objects.nonNull(configs)) {
            return configs.path(ID).asInt(this.getDefaultValue());
        }
        return this.getDefaultValue();
    }

    @Override
    public boolean validate(Integer value) {
        return Objects.nonNull(value) && value > 0;
    }
}
