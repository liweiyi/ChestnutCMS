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

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginSecurity {

    /**
     * 触发密码重试安全策略的次数上限
     */
    private Integer passwordRetryLimit;

    /**
     * 密码重试安全策略<br/>
     * @see com.chestnut.system.fixed.dict.PasswordRetryStrategy
     */
    private String passwordRetryStrategy;

    /**
     * 密码重试安全策略锁定时长，单位：秒
     */
    private Integer passwordRetryLockSeconds;

    /**
     * 验证码是否启用
     */
    private String captchaEnable;

    /**
     * 验证码类型
     */
    private String captchaType;

    /**
     * 验证码过期时长，单位：秒
     */
    private Integer captchaExpires;

    /**
     * 验证码重新生成间隔时长，单位：秒
     */
    private Integer captchaDuration;

    /**
     * 验证码消息配置ID。
     */
    private Long captchaMessageConfigId;

    /**
     * 验证码消息模板ID。
     */
    private Long captchaMessageTemplateId;
}
