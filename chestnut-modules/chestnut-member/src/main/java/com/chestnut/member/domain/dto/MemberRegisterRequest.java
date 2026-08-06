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

import com.chestnut.common.validation.RegexConsts;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import com.chestnut.common.annotation.XComment;

@Getter
@Setter
@XComment("{API.DOC.MEMBER.REGISTER_REQ}")
public class MemberRegisterRequest {
	
	/**
	 * 注册方式：邮箱注册，手机号注册，手机短信验证码注册
	 */
	@XComment("{API.DOC.MEMBER.REGISTER_TYPE}")
	@NotBlank
	@Length(max = 20)
	private String type;
	
	/**
	 * 用户名
	 */
	@XComment("{API.DOC.MEMBER.USERNAME}")
	@Length(min = 6, max = 30)
	@Pattern(regexp = RegexConsts.REGEX_USERNAME)
	private String userName;
	
	/**
	 * 邮箱
	 */
	@XComment("{API.DOC.MEMBER.EMAIL}")
	@Email
	private String email;
	
	/**
	 * 手机号
	 */
	@XComment("{API.DOC.MEMBER.PHONE}")
	@Pattern(regexp = RegexConsts.REGEX_PHONE, message = "{VALID.MEMBER.PHONE_PATTERN}")
	private String phonenumber;

	/**
	 * 用户密码
	 */
	@XComment("{API.DOC.MEMBER.PASSWORD}")
	@NotBlank
	@Length(max = 100)
	private String password;

	/**
	 * 验证码
	 */
	@XComment("{API.DOC.MEMBER.AUTH_CODE}")
	@Length(max = 10)
	private String authCode;

	@XComment("{CC.MEMBER.IP}")
	private String ip;

	@XComment("{CC.MEMBER.USER_AGENT}")
	private String userAgent;
}
