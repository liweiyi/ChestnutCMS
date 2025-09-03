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
package com.chestnut.member.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * UpdateMemberRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class UpdateMemberRequest extends BaseDTO {

    @LongId
    private Long memberId;

    /**
     * 会员用户名
     */
    @NotBlank
    @Length(min = 2, max = 30)
    @Pattern(regexp = RegexConsts.REGEX_USERNAME)
    private String userName;

    /**
     * 昵称
     */
    @Length(max = 30)
    private String nickName;

    /**
     * 手机号
     */
    @Length(max = 20)
    @Pattern(regexp = RegexConsts.REGEX_PHONE)
    private String phoneNumber;

    /**
     * Email
     */
    @Email
    @Length(max = 50)
    private String email;

    /**
     * 出生日期
     */
    private LocalDateTime birthday;

    /**
     * 状态
     */
    @Length(max = 1)
    @Dict(MemberStatus.TYPE)
    private String status;

    /**
     * 备注
     */
    @Length(max = 500)
    private String remark;
}
