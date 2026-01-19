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
package com.chestnut.search.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateSearchWordRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateSearchWordRequest extends BaseDTO {

    /**
     * 搜索词
     */
    @NotBlank
    @Length(max = 255)
    private String word;

    /**
     * 历史总搜索次数
     */
    @Min(0)
    private Long searchTotal;

    /**
     * 来源标识，例如：cms:siteId
     */
    private String source;
}
