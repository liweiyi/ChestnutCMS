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

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.FOLLOW_CANCEL_REQ}")
public class CancelMemberFollowRequest {

    @NotNull
    @LongId
    @XComment("{API.DOC.MEMBER.MEMBER_ID}")
    private Long memberId;

    @NotBlank
    @Pattern(regexp = "follow|follower")
    @XComment("{API.DOC.MEMBER.FOLLOW_TYPE}")
    private String type;

    @NotNull
    @LongId
    @XComment("{API.DOC.MEMBER.TARGET_ID}")
    private Long targetMemberId;
}
