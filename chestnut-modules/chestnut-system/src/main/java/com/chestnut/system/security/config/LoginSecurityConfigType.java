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

import com.chestnut.common.captcha.math.MathCaptchaType;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.fixed.dict.PasswordRetryStrategy;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.ISecurityUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Component(ISecurityConfigType.BEAN_PREFIX + LoginSecurityConfigType.TYPE)
public class LoginSecurityConfigType implements ISecurityConfigType<LoginSecurity> {

    public static final String TYPE = "Login";

    private final static String CACHE_KEY_PWD_RETRY = "sys:security:pwdretry:";

    private final RedisCache redisCache;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public LoginSecurity getConfig(ObjectNode configs) {
        JsonNode node = configs.get(TYPE);
        if (Objects.isNull(node)) {
            return new LoginSecurity();
        }
        return JacksonUtils.convertValue(node, LoginSecurity.class);
    }

    @Override
    public void fixedOldVersion(SysSecurityConfig securityConfig) {
        ObjectNode configs = securityConfig.getConfigs();
        if (configs.has(TYPE)) {
            return; // 已处理过
        }
        ObjectNode jsonNode = JacksonUtils.objectNode();
        jsonNode.put("passwordRetryLimit", Objects.requireNonNullElse(securityConfig.getPasswordRetryLimit(), 0));
        jsonNode.put("passwordRetryStrategy", Objects.requireNonNullElse(securityConfig.getPasswordRetryStrategy(), ""));
        jsonNode.put("passwordRetryLockSeconds", Objects.requireNonNullElse(securityConfig.getPasswordRetryLockSeconds(), 3600));
        jsonNode.put("captchaEnable", Objects.requireNonNullElse(securityConfig.getCaptchaEnable(), YesOrNo.NO));
        jsonNode.put("captchaType", Objects.requireNonNullElse(securityConfig.getCaptchaType(), MathCaptchaType.TYPE));
        jsonNode.put("captchaEnable", Objects.requireNonNullElse(securityConfig.getCaptchaExpires(), 600));
        jsonNode.put("captchaEnable", Objects.requireNonNullElse(securityConfig.getCaptchaDuration(), 0));
        ArrayNode loginTypeConfigIds = JacksonUtils.arrayNode();
        if (StringUtils.isNotEmpty(securityConfig.getLoginTypeConfigIds())) {
            securityConfig.getLoginTypeConfigIds().forEach(loginTypeConfigIds::add);
        }
        jsonNode.set("loginTypeConfigIds", loginTypeConfigIds);
        configs.set(TYPE, jsonNode);
    }

    public void processLoginPasswordError(LoginSecurity config, ISecurityUser user) {
        String cacheKey = user.getType() + "_" + user.getUserId();
        // 缓存更新
        LoginPwdRetry lpe = this.redisCache.getCacheMapValue(CACHE_KEY_PWD_RETRY, cacheKey);
        if (Objects.isNull(lpe)) {
            lpe = new LoginPwdRetry(cacheKey);
        }
        lpe.inc();
        this.redisCache.setCacheMapValue(CACHE_KEY_PWD_RETRY, cacheKey, lpe);
        // 执行策略
        int passwordRetryLimit = config.getPasswordRetryLimit();
        if (passwordRetryLimit > 0 && lpe.getNum() >= passwordRetryLimit) {
            // 达到指定次数上限触发安全策略
            if (PasswordRetryStrategy.DISABLE.equals(config.getPasswordRetryStrategy())) {
                user.disableUser();
            } else if (PasswordRetryStrategy.LOCK.equals(config.getPasswordRetryStrategy())) {
                LocalDateTime lockEndTime = LocalDateTime.now().plusSeconds(config.getPasswordRetryLockSeconds());
                user.lockUser(lockEndTime);
            }
        }
    }

    public void onLoginSuccess(ISecurityUser user) {
        this.redisCache.deleteCacheMapValue(CACHE_KEY_PWD_RETRY, user.getType() + "_" + user.getUserId());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class LoginPwdRetry {
        private String uid;
        private Integer num = 0;
        private LocalDate date = LocalDate.now();

        public LoginPwdRetry(String uid) {
            this.uid = uid;
        }

        public void inc() {
            LocalDate now = LocalDate.now();
            if (!now.isEqual(this.date)) {
                this.num = 0;
            }
            this.date = now;
            this.num++;
        }
    }
}
