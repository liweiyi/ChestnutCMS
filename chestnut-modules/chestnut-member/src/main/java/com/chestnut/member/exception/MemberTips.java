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
package com.chestnut.member.exception;

import com.chestnut.common.i18n.I18nUtils;

import java.util.Locale;

public enum MemberTips {

    /**
     * 密码错误
     */
    WRONG_PASSWORD;

    public String locale(Object... args) {
        return I18nUtils.parse("TIPS.MEMBER." + this.name(), args);
    }

    public String locale(Locale locale, Object... args) {
        return I18nUtils.parse("TIPS.MEMBER." + this.name(), locale, args);
    }
}