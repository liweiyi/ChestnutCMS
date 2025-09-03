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
package com.chestnut.vote.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * SaveSubjectItemsRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SaveSubjectItemsRequest extends BaseDTO {

    /**
     * 主题ID
     */
    @LongId
    private Long subjectId;

    /**
     * 问卷调查主题选项列表
     */
    @Valid
    private List<SubjectItem> itemList;

    @Getter
    @Setter
    public static class SubjectItem {

        private Long itemId;

        /**
         * 类型（文字、图片、内容引用）
         */
        @NotBlank
        @Length(max = 20)
        private String type;

        /**
         * 选项内容（文字内容、图片地址、内容引用地址）
         */
        @Length(max = 255)
        private String content;

        /**
         * 选项描述
         */
        @Length(max = 255)
        private String description;
    }
}
