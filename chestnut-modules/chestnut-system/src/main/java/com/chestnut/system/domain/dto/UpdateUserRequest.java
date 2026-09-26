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
package com.chestnut.system.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * UpdateUserRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.SYS.UPDATE_USER_REQ}")
public class UpdateUserRequest extends BaseDTO {

    @LongId
    @XComment("{ENT.SYS.USER.ID}")
    private Long userId;

    @Length(max = 30)
    @XComment("{ENT.SYS.USER.NICK_NAME}")
    private String nickName;

    @Length(max = 30)
    @XComment("{ENT.SYS.USER.REAL_NAME}")
    private String realName;

    @Email
    @Length(max = 50)
    @XComment("{ENT.SYS.USER.MAIL}")
    private String email;

    @Length(max = 20)
    @XComment("{ENT.SYS.USER.PHONE}")
    private String phoneNumber;

    @NotBlank
    @Dict(value = Gender.TYPE)
    @XComment("{ENT.SYS.USER.GENDER}")
    private String sex;

    @XComment("{ENT.SYS.USER.BIRTHDAY}")
    private LocalDateTime birthday;

    @Dict(UserStatus.TYPE)
    @XComment("{ENT.SYS.USER.STATUS}")
    private String status;

    @XComment("{ENT.SYS.USER.POST_IDS}")
    private Long[] postIds;

    @Length(max = 500)
    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
}
