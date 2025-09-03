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
package com.chestnut.system.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.system.fixed.dict.UserStatus;
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
 * CreateUserRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CreateUserRequest extends BaseDTO {

    @LongId
    private Long deptId;

    @NotBlank
    @Length(max = 30)
    @Pattern(regexp = RegexConsts.REGEX_USERNAME)
    private String userName;

    @Length(max = 30)
    private String nickName;

    @Length(max = 30)
    private String realName;

    @Email
    @Length(max = 50)
    private String email;

    @Length(max = 20)
    private String phoneNumber;

    @NotBlank
    @Dict(value = Gender.TYPE)
    private String sex;

    private LocalDateTime birthday;

    @NotBlank
    private String password;

    @Dict(UserStatus.TYPE)
    private String status;

    private Long[] postIds;

    @Length(max = 500)
    private String remark;
}
