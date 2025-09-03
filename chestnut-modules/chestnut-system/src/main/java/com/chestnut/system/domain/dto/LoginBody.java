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

import com.chestnut.common.captcha.CaptchaData;
import com.chestnut.common.validation.RegexConsts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录对象
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class LoginBody {
	
	/**
	 * 用户名
	 */
	@NotBlank
	@Pattern(regexp = RegexConsts.REGEX_USERNAME)
	private String username;

	/**
	 * 用户密码
	 */
	@NotBlank
	private String password;

	/**
	 * 验证码信息
	 */
	private CaptchaData captcha;
}
