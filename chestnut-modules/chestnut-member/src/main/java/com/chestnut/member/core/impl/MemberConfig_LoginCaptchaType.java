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

import com.chestnut.common.captcha.text.TextCaptchaType;
import com.chestnut.common.utils.StringUtils;
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
@Component(IMemberConfig.BEAN_PREFIX + MemberConfig_LoginCaptchaType.ID)
public class MemberConfig_LoginCaptchaType implements IMemberConfig<String> {

    public static final String ID = "loginCaptchaType";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{MEMBER.CONFIG.LOGIN_CAPTCHA_TYPE}";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public String getDefaultValue() {
        return TextCaptchaType.TYPE;
    }

    @Override
    public MemberConfigControlType getControlType() {
        return MemberConfigControlType.CAPTCHA_SELECTOR;
    }

    @Override
    public int getOrder() {
        return 11;
    }

    @Override
    public String getValue(ObjectNode configs) {
        if (Objects.nonNull(configs)) {
            return configs.path(ID).asString(this.getDefaultValue());
        }
        return this.getDefaultValue();
    }

    @Override
    public boolean validate(String value) {
        return StringUtils.isNotBlank(value);
    }
}
