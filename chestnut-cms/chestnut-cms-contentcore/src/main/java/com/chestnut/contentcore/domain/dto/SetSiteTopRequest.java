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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.annotation.XComment;
import jakarta.validation.constraints.Future;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 站点置顶请求
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.CMS.SITE.SET_TOP}")
public class SetSiteTopRequest {

    /**
     * 置顶结束时间，空值表示持续置顶
     */
    @Future
    @XComment("{API.DOC.CMS.CONTENT.TOP_END_TIME}")
    private LocalDateTime topEndTime;
}
