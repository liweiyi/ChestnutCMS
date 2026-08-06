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
package com.chestnut.cms.cdn.exception;

import com.chestnut.common.exception.ErrorCode;

public enum CdnErrorCode implements ErrorCode {

    /**
     * 未配置CDN云服务
     */
    CDN_CLOUD_NOT_CONFIGURED,

    /**
     * 云服务配置不存在
     */
    CLOUD_CONFIG_NOT_FOUND;

    @Override
    public String value() {
        return "{ERRCODE.CMS.CDN." + this.name() + "}";
    }
}
