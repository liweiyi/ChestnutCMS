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
package com.chestnut.common.utils;

import com.chestnut.common.validation.RegexConsts;

import java.util.regex.Pattern;

/**
 * ValidateUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ValidateUtils {

    public static final Pattern PatternChineseMainlandPhoneNumber = Pattern.compile(RegexConsts.REGEX_PHONE);

    /**
     * 校验中国大陆手机号码
     *
     * @param phoneNumber 手机号码
     */
    public static boolean validateChineseMainlandPhoneNumber(String phoneNumber) {
        return PatternChineseMainlandPhoneNumber.matcher(phoneNumber).matches();
    }
}
