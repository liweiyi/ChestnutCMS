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
package com.chestnut.system.security.config;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.fixed.dict.PasswordRule;
import com.chestnut.system.fixed.dict.YesOrNo;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component(ISecurityConfigType.BEAN_PREFIX + PasswordSecurityConfigType.TYPE)
public class PasswordSecurityConfigType implements ISecurityConfigType<PasswordSecurity> {

    public static final String TYPE = "Password";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PasswordSecurity getConfig(ObjectNode configs) {
        JsonNode node = configs.get(TYPE);
        if (Objects.isNull(node)) {
            return new PasswordSecurity();
        }
        PasswordSecurity config = JacksonUtils.convertValue(node, PasswordSecurity.class);
        String ruleRegex = PasswordRule.getRuleRegex(config.getRule());
        config.setRulePattern(ruleRegex);
        return config;
    }

    @Override
    public void fixedOldVersion(SysSecurityConfig securityConfig) {
        ObjectNode configs = securityConfig.getConfigs();
        if (configs.has(TYPE)) {
            return; // 已处理过
        }
        ObjectNode jsonNode = JacksonUtils.objectNode();
        jsonNode.put("minLength", Objects.requireNonNullElse(securityConfig.getPasswordLenMin(), 6));
        jsonNode.put("maxLength", Objects.requireNonNullElse(securityConfig.getPasswordLenMax(), 16));
        jsonNode.put("rule", securityConfig.getPasswordRule());
        ArrayNode arrayNodeSensitives = JacksonUtils.arrayNode();
        if (Objects.nonNull(securityConfig.getPasswordSensitive())) {
            for (String sensitive : securityConfig.getPasswordSensitive()) {
                arrayNodeSensitives.add(sensitive);
            }
        }
        jsonNode.set("sensitives", arrayNodeSensitives);
        jsonNode.put("expireSeconds", Objects.requireNonNullElse(securityConfig.getPasswordExpireSeconds(), 0));
        ArrayNode arrayNodeWeakPasswords = JacksonUtils.arrayNode();
        if (StringUtils.isNotBlank(securityConfig.getWeakPasswords())) {
            String[] split = securityConfig.getWeakPasswords().split("\n");
            for (String str : split) {
                arrayNodeWeakPasswords.add(str);
            }
        }
        jsonNode.set("weakPasswords", arrayNodeWeakPasswords);
        jsonNode.put("forceModifyPwdAfterAdd", Objects.requireNonNullElse(securityConfig.getForceModifyPwdAfterAdd(), YesOrNo.NO));
        jsonNode.put("forceModifyPwdAfterReset", Objects.requireNonNullElse(securityConfig.getForceModifyPwdAfterReset(), YesOrNo.NO));
        configs.set(TYPE, jsonNode);
    }
}
