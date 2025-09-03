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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * 会员登录DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class MemberLoginRequest {

    /**
     * 登录方式（Email, 手机号， 手机验证码，用户名）
     */
    @NotBlank
    @Length(max = 20)
    private String type;

    @NotBlank
    @Length(max = 30)
    private String username;

    @NotBlank
    @Length(max = 100)
    private String password;

    @Length(max = 10)
    private String code;

    @Length(max = 100)
    private String uuid;

    private String ip;

    private String userAgent;
}
