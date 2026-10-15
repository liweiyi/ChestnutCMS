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
package com.chestnut.common.captcha;

import jakarta.annotation.Nonnull;

import java.util.concurrent.TimeUnit;

/**
 * 验证码类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICaptchaType {

    String BEAN_PREFIX = "CaptchaType_";

    /**
     * 自定义验证码过期时间属性KEY
     */
    String EXPIRE_SECONDS_PROP_KEY = "expireSeconds";

    String getType();

    String getName();

    /**
     * 获取验证码存储实现
     */
    ICaptchaStorage getStorage();

    /**
     * 获取验证码缓存键名
     *
     * @param scene 业务场景标识
     * @param token 验证码归属标识
     * @return 缓存键名
     */
    default String getCacheKey(@Nonnull String scene, @Nonnull String token) {
        return "cc:captcha:" + getType().toLowerCase() + ":" + scene + ":" + token.trim();
    }

    default String getSuccessCacheKey(@Nonnull String scene, @Nonnull String token) {
        return "cc:captcha:success:" + getType().toLowerCase() + ":" + scene + ":" + token;
    }

    /**
     * 校验验证码发送频率
     *
     * @param expireSeconds 验证码过期时间
     * @param captchaDuration 验证码发送频率
     */
    default void checkLimit(String scene, String token, long expireSeconds, int captchaDuration) {
        String cacheKey = this.getCacheKey(scene, token);
        long expire = this.getStorage().getExpires(cacheKey, TimeUnit.SECONDS);
        if (expire > 0 && expireSeconds - expire < captchaDuration) {
            throw CaptchaErrorCode.CAPTCHA_LIMIT.exception();
        }
    }

    /**
     * 创建验证码
     */
    Object create(CaptchaData captchaData);

    /**
     * 校验验证码
     *
     * @param captchaData 验证码校验信息
     */
    CaptchaCheckResult check(CaptchaData captchaData);

    /**
     * 指定Token是否有已通过校验
     *
     * @param captchaData 验证码校验信息
     * @return 结果
     */
    boolean isTokenValidated(CaptchaData captchaData);
}
