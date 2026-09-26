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
package com.chestnut.cloud.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.validator.LongId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.QUERY_REQ}")
public class QueryCdnRefreshLogRequest {

    @LongId
    @XComment("{API.DOC.CLOUD.CONFIG_ID}")
    private Long configId;

    @NotBlank
    @Pattern(regexp = "url|directory|preheat")
    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.QUERY_TYPE}")
    private String type = "url";

    @XComment("{ENT.CLOUD.CDN_REFRESH_LOG.URL}")
    private String url;

    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.BEGIN_TIME}")
    private LocalDateTime beginTime;

    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.END_TIME}")
    private LocalDateTime endTime;

    @Min(1)
    @Max(100000)
    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.PAGE_NUM}")
    private int pageNum = 1;

    @Min(1)
    @Max(100)
    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.PAGE_SIZE}")
    private int pageSize = 10;

    @JsonIgnore
    @AssertTrue(message = "{ERR.CLOUD.CDN_LOG_TIME_RANGE}")
    public boolean isTimeRangeValid() {
        if (beginTime == null && endTime == null) {
            return true;
        }
        return beginTime != null && endTime != null && beginTime.isBefore(endTime);
    }
}
