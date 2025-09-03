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
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import com.chestnut.vote.fixed.dict.VoteSubjectType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateVoteSubjectRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateVoteSubjectRequest extends BaseDTO {

    @LongId
    private Long voteId;

    /**
     * 类型（单选、多选、输入）
     */
    @NotBlank
    @Length(max = 20)
    @Dict(VoteSubjectType.TYPE)
    private String type;

    /**
     * 标题
     */
    @NotBlank
    @Length(max = 255)
    private String title;

    /**
     * 排到指定主题前
     */
    private Long nextSubjectId;
}
