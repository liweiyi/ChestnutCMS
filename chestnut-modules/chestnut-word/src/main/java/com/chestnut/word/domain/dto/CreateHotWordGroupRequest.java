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
package com.chestnut.word.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateHotWordGroupRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.WORD.HOT_WORD_GROUP.CREATE_REQ}")
public class CreateHotWordGroupRequest extends BaseDTO {

    @XComment("{CC.HOT_WORD_GROUP.OWNER}")
    @Length(max = 100)
    private String owner;

    @XComment("{CC.HOT_WORD_GROUP.NAME}")
    @NotBlank
    @Length(max = 255)
    private String name;

    @XComment("{CC.HOT_WORD_GROUP.CODE}")
    @NotBlank
    @Length(max = 50)
    @Pattern(regexp = RegexConsts.REGEX_CODE)
    private String code;

    @XComment("{CC.ENTITY.SORT}")
    @NotNull
    private Long sortFlag;

    @XComment("{CC.ENTITY.REMARK}")
    @Length(max = 500)
    private String remark;
}
