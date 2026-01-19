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
package com.chestnut.member.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateMemberExpConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateMemberExpConfigRequest extends BaseDTO {

    /**
     * 操作项ID
     */
    @NotBlank
    @Length(max = 50)
    private String opType;

    /**
     * 积分类型
     */
    @NotBlank
    @Length(max = 30)
    private String levelType;

    /**
     * 积分变更值
     */
    @NotNull
    private Integer exp;

    /**
     * 每日生效次数上限
     */
    @NotNull
    @Min(0)
    private Integer dayLimit;

    /**
     * 总生效次数上限
     */
    @NotNull
    @Min(0)
    private Integer totalLimit;

    /**
     * 备注
     */
    @Length(max = 500)
    private String remark;
}
