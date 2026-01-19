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
package com.chestnut.comment.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class SubmitCommentDTO extends BaseDTO {

    /**
     * 评论目标类型
     */
    @NotBlank
    @Length(max = 20)
    private String sourceType;

    /**
     * 评论目标ID
     */
    @NotBlank
    @Length(max = 64)
    private String sourceId;

    /**
     * 回复的评论ID
     */
    @Min(0)
    @NotNull
    private Long commentId;

    /**
     * 回复的用户UID
     */
    @Min(0)
    @NotNull
    private Long replyUid;

    /**
     * 评论内容
     */
    @NotBlank
    @Length(max = 2000)
    private String content;

    /**
     * IP
     */
    private String clientIp;

    /**
     * UA
     */
    private String userAgent;
}
