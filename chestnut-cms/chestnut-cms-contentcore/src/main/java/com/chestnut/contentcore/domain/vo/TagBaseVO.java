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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * TagBaseVO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagBaseVO {

    @XComment("创建者")
    private String createBy;

    @XComment("创建时间")
    private LocalDateTime createTime;

    @XComment("更新者")
    private String updateBy;

    @XComment("更新时间")
    private LocalDateTime updateTime;

    @XComment("备注")
    private String remark;
}
