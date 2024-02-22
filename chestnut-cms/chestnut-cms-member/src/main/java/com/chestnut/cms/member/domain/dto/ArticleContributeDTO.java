/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.cms.member.domain.dto;

import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 文章投稿 DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ArticleContributeDTO {

    private Long contentId;

    /**
     * 分类ID
     */
    @LongId
    private Long catalogId;

    /**
     * 标题
     */
    @NotBlank
    @Length(max = 120)
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 文章标签
     */
    private List<String> tags;

    /**
     * 正文内容
     */
    @NotBlank
    private String contentHtml;

    /**
     * 引导图
     */
    private String logo;
}
