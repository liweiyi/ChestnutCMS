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
package com.chestnut.vote.domain.dto;

import com.chestnut.common.annotation.XComment;
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
@XComment("{API.DOC.VOTE.SUBJECT.CREATE_REQ}")
public class CreateVoteSubjectRequest extends BaseDTO {

    @LongId
    @XComment("{CC.VOTE_SUBJECT.VOTE_ID}")
    private Long voteId;

    @NotBlank
    @Length(max = 20)
    @Dict(VoteSubjectType.TYPE)
    @XComment("{CC.VOTE_SUBJECT.TYPE}")
    private String type;

    @NotBlank
    @Length(max = 255)
    @XComment("{CC.VOTE_SUBJECT.TITLE}")
    private String title;

    @XComment("{CC.VOTE_SUBJECT.NEXT_SUBJECT_ID}")
    private Long nextSubjectId;
}
