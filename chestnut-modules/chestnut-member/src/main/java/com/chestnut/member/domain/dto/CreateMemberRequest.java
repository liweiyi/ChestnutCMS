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
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * CreateMemberRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MEMBER.CREATE_REQ}")
public class CreateMemberRequest extends BaseDTO {

    @NotBlank
    @Length(min = 2, max = 30)
    @Pattern(regexp = RegexConsts.REGEX_USERNAME)
    @XComment("{API.DOC.MEMBER.USERNAME}")
    private String userName;

    @NotBlank
    @Length(max = 100)
    @XComment("{API.DOC.MEMBER.PASSWORD}")
    private String password;

    @Length(max = 30)
    @XComment("{API.DOC.MEMBER.NICK_NAME}")
    private String nickName;

    @Length(max = 20)
    @Pattern(regexp = RegexConsts.REGEX_PHONE)
    @XComment("{API.DOC.MEMBER.PHONE}")
    private String phoneNumber;

    @Email
    @Length(max = 50)
    @XComment("{API.DOC.MEMBER.EMAIL}")
    private String email;

    @XComment("{API.DOC.MEMBER.BIRTHDAY}")
    private LocalDateTime birthday;

    @Length(max = 1)
    @Dict(MemberStatus.TYPE)
    @XComment("{CC.ENTITY.STATUS}")
    private String status;

    @Length(max = 500)
    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
}
