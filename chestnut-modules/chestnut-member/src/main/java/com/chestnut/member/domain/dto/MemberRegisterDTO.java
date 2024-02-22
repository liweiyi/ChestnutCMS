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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class MemberRegisterDTO {
	
	/**
	 * 注册方式：邮箱注册，手机号注册，手机短信验证码注册
	 */
	@NotEmpty
	private String type;
	
	/**
	 * 用户名
	 */
	@Size(min = 6, max = 30, message = "用户名长度必须大于6且小于30个字符")
	@Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "用户名必须以字母开头，且只能为（大小写字母，数字，下滑线）")
	private String userName;
	
	/**
	 * 邮箱
	 */
	@Email
	private String email;
	
	/**
	 * 手机号
	 */
	@Pattern(regexp = "^1[3|4|5|6|7|8|9][0-9]\\d{8}$", message = "手机号码格式错误")
	private String phonenumber;

	/**
	 * 用户密码
	 */
	@NotEmpty
	private String password;

	/**
	 * 验证码
	 */
	private String authCode;

	private String ip;

	private String userAgent;
}
