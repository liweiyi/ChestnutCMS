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
package com.chestnut.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * 会员登录DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@Validated
public class MemberLoginDTO {

    /**
     * 登录方式（Email, 手机号， 手机验证码，用户名）
     */
    @NotEmpty
    private String type;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String code;

    private String uuid;

    private String ip;

    private String userAgent;
}
