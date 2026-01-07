/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

/**
 * 验证码类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICaptchaType {

    String BEAN_PREFIX = "CaptchaType_";

    String getType();

    String getName();

    default String getCacheKey(String token) {
        return "cc:captcha:" + getType().toLowerCase() + ":" + token;
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
