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
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateSensitiveWordRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.WORD.SENSITIVE_WORD.CREATE_REQ}")
public class CreateSensitiveWordRequest extends BaseDTO {

    @XComment("{CC.SENSITIVE_WORD.TYPE}")
    @NotBlank
    @Length(max = 5)
    private String type;

    @XComment("{CC.SENSITIVE_WORD.WORD}")
    @NotBlank
    @Length(max = 255)
    private String word;

    @XComment("{CC.SENSITIVE_WORD.REPLACE_WORD}")
    @Length(max = 255)
    private String replaceWord;

    @XComment("{CC.ENTITY.REMARK}")
    @Length(max = 500)
    private String remark;
}
