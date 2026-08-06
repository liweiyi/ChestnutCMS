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
package com.chestnut.common.cloud;

import lombok.Getter;
import lombok.Setter;

/**
 * DNS解析记录
 */
@Getter
@Setter
public class DnsRecord {

    private String rr;

    private String type;

    private String value;

    /**
     * 解析记录是否启用，null 视为启用
     */
    private Boolean enabled;

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public static boolean parseEnableStatus(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return "ENABLE".equalsIgnoreCase(status.trim());
    }
}
