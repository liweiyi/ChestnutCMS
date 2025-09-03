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
package com.chestnut.system.exception;

import com.chestnut.common.i18n.I18nUtils;

import java.util.Locale;

public enum SystemUserTips {

    /**
     * 账号“{0}”部门不存在。
     */
    USER_DEPT_NOT_EXISTS,

    /**
     * 账号“{0}”已存在。
     */
    USER_EXISTS,

    /**
     * 账号“{0}”导入失败：{1}
     */
    IMPORT_FAIL,

    /**
     * 导入完成，成功数：{0}，失败数：{1}
     */
    IMPORT_SUCCESS;

    public String locale(Object... args) {
        return I18nUtils.parse("TIPS.SYS.USER." + this.name(), args);
    }

    public String locale(Locale locale, Object... args) {
        return I18nUtils.parse("TIPS.SYS.USER." + this.name(), locale, args);
    }
}
