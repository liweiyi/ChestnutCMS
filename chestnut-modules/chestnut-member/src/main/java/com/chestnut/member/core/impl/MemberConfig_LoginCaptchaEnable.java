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
 * 会员登录验证码是否开启配置。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberConfig.BEAN_PREFIX + MemberConfig_LoginCaptchaEnable.ID)
public class MemberConfig_LoginCaptchaEnable implements IMemberConfig<Boolean> {

    public static final String ID = "loginCaptcha";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{MEMBER.CONFIG.LOGIN_CAPTCHA_ENABLE}";
    }

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }

    @Override
    public MemberConfigControlType getControlType() {
        return MemberConfigControlType.SWITCH;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public Boolean getValue(ObjectNode configs) {
        if (Objects.nonNull(configs)) {
            return configs.path(ID).asBoolean(false);
        }
        return this.getDefaultValue();
    }

    @Override
    public boolean validate(Boolean value) {
        return Objects.nonNull(value);
    }
}
