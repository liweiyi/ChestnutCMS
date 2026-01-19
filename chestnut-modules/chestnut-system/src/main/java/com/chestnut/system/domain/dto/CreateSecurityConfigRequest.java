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
package com.chestnut.system.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.PasswordRetryStrategy;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * CreateSecurityConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateSecurityConfigRequest extends BaseDTO {

    /**
     * 密码最小长度
     */
    @Min(1)
    @Max(100)
    private Integer passwordLenMin;

    /**
     * 密码最大长度
     */
    @Min(1)
    @Max(100)
    private Integer passwordLenMax;

    /**
     * 密码校验规则，默认：包含字母数字
     */
    @Length(max = 50)
    private String passwordRule;

    /**
     * 密码中不允许包含的用户信息
     */
    @Size(max = 20)
    private String[] passwordSensitive;

    /**
     * 禁用弱密码数组<br/>
     * 例如：123456, 666666, qweqwe等常见密码
     */
    @Length(max = 500)
    private String weakPasswords;

    /**
     * 后台添加的用户首次登陆是否需要强制修改密码
     */
    @NotBlank
    @Dict(YesOrNo.TYPE)
    private String forceModifyPwdAfterAdd;

    /**
     * 后台重置密码后首次登陆是否需要强制修改密码
     */
    @NotBlank
    @Dict(YesOrNo.TYPE)
    private String forceModifyPwdAfterReset;

    /**
     * 密码过期时间长度，单位：秒，0表示永不过期，最长不超过100天
     */
    @Min(0)
    @Max(8640000)
    private Integer passwordExpireSeconds;

    /**
     * 触发密码重试安全策略的次数上限
     */
    @Min(0)
    private Integer passwordRetryLimit;

    /**
     * 密码重试安全策略
     */
    @NotBlank
    @Dict(PasswordRetryStrategy.TYPE)
    private String passwordRetryStrategy;

    /**
     * 密码重试安全策略锁定时长，单位：秒，最长不超过365天
     */
    @Min(0)
    @Max(31536000)
    private Integer passwordRetryLockSeconds;

    /**
     * 验证码是否启用
     */
    @Dict(YesOrNo.TYPE)
    private String captchaEnable;

    /**
     * 验证码类型
     */
    private String captchaType;

    /**
     * 验证码过期时长，单位：秒
     */
    @Min(10)
    @Max(3600)
    private Integer captchaExpires;

    /**
     * 验证码重新生成间隔时长，单位：秒
     */
    @Min(0)
    private Integer captchaDuration;

    @NotNull
    private List<Long> loginTypeConfigIds;
}
