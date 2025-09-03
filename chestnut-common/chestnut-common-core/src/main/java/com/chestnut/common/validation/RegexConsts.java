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
package com.chestnut.common.validation;

public interface RegexConsts {

    /**
     * 通用用户名校验正则表达式
     */
    String REGEX_USERNAME = "^[A-Za-z][A-Za-z0-9_]+$";

    /**
     * 通用编码校验正则表达式
     */
    String REGEX_CODE = "^[A-Za-z0-9_]+$";

    /**
     * 通用路径校验正则表达式
     */
    String REGEX_PATH = "^[A-Za-z0-9_\\/]+$";

    /**
     * 通用文件名校验正则表达式
     */
    String REGEX_FILE_NAME = "^[A-Za-z0-9_\\.]+$";

    /**
     * 通用文件名校验正则表达式
     */
    String REGEX_PHONE = "^1[3|4|5|6|7|8|9][0-9]\\d{8}$";
}
