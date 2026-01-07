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
package com.chestnut.xmodel.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CreateXModelFieldRequest extends BaseDTO {

    @LongId
    private Long modelId;

    @NotBlank
    @Length(max = 50)
    private String name;

    @NotBlank
    @Length(max = 50)
    @Pattern(regexp = RegexConsts.REGEX_CODE)
    private String code;

    @Length(max = 20)
    private String fieldType;

    private String fieldName;

    @NotBlank
    @Length(max = 20)
    private String controlType;

    private List<Map<String, Object>> validations;

    private FieldOptions options;

    @Length(max = 100)
    private String defaultValue;

    @NotNull
    private Long sortFlag;

    @Length(max = 500)
    private String remark;
}
