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

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.PasswordRule;
import com.chestnut.system.fixed.dict.PasswordSensitive;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.ISecurityUser;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PasswordSecurity {

    /**
     * 密码最小长度
     */
    private Integer minLength = 6;

    /**
     * 密码最大长度
     */
    private Integer maxLength = 16;

    /**
     * 密码校验规则，默认：包含字母数字<br/>
     */
    private String rule;

    /**
     * 密码校验规则正则表达式
     */
    private String rulePattern;

    /**
     * 密码中不允许包含的用户信息<br/>
     */
    private List<String> sensitives;

    /**
     * 禁用弱密码数组<br/>
     * 例如：123456, 666666, qweqwe等常见密码
     */
    private List<String> weakPasswords;

    /**
     * 后台添加的用户首次登陆是否需要强制修改密码
     */
    private String forceModifyPwdAfterAdd;

    /**
     * 后台重置密码后首次登陆是否需要强制修改密码
     */
    private String forceModifyPwdAfterReset;

    /**
     * 密码过期时间长度，单位：秒
     */
    private Long expireSeconds = 0L;

    public void validPassword(ISecurityUser user, String password) {
        // 最大长度
        boolean valid = this.getMaxLength() > 0
                && password.length() <= this.getMaxLength();
        Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
        // 最小长度
        valid = this.getMinLength() > 0 && password.length() >= this.getMinLength();
        Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
        // 校验规则检查
        valid = PasswordRule.match(this.getRule(), password);
        Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
        // 敏感字符检查
        valid = PasswordSensitive.check(this.getSensitives(), password, user);
        Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
        // 弱密码检查
        valid = StringUtils.isEmpty(this.getWeakPasswords()) || !this.getWeakPasswords().contains(password);
        Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
    }

    public void checkForceModifyPwdAfterReset(ISecurityUser user) {
        if (YesOrNo.isYes(this.getForceModifyPwdAfterReset())) {
            user.forceModifyPassword();
        }
    }

    public void checkForceModifyPwdAfterAdd(ISecurityUser user) {
        if (YesOrNo.isYes(this.getForceModifyPwdAfterAdd())) {
            user.forceModifyPassword();
        }
    }
}
