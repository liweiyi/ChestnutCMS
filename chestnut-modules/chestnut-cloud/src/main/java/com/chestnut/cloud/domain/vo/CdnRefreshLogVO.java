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
package com.chestnut.cloud.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Locale;

@Getter
@AllArgsConstructor
@XComment("{ENT.CLOUD.CDN_REFRESH_LOG.ENTITY}")
public class CdnRefreshLogVO {

    @XComment("{ENT.CLOUD.CDN_REFRESH_LOG.URL}")
    private final String url;

    @XComment("{ENT.CLOUD.CDN_REFRESH_LOG.TASK_TYPE}")
    private final String taskType;

    @XComment("{ENT.CLOUD.CDN_REFRESH_LOG.TYPE}")
    private final String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @XComment("{ENT.CLOUD.CDN_REFRESH_LOG.TIME}")
    private final LocalDateTime time;

    @XComment("{ENT.CLOUD.CDN_REFRESH_LOG.STATUS}")
    private final Status status;

    public enum Status {
        PROCESSING, SUCCESS, FAILED, TIMEOUT, CANCELED, INVALID, UNKNOWN;

        public static Status fromCloudStatus(String status) {
            if (status == null) {
                return UNKNOWN;
            }
            return switch (status.toLowerCase(Locale.ROOT)) {
                case "refreshing", "process" -> PROCESSING;
                case "complete", "done" -> SUCCESS;
                case "failed", "fail" -> FAILED;
                case "timeout" -> TIMEOUT;
                case "canceled" -> CANCELED;
                case "invalid" -> INVALID;
                default -> UNKNOWN;
            };
        }
    }
}
