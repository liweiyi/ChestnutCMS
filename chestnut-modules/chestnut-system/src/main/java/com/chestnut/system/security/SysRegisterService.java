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
package com.chestnut.system.security;

import com.chestnut.common.captcha.CaptchaData;
import com.chestnut.common.captcha.CaptchaService;
import com.chestnut.common.captcha.ICaptchaType;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.RegisterBody;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.config.SysCaptchaEnable;
import com.chestnut.system.fixed.config.SysRegistEnable;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.service.ISysLogininforService;
import com.chestnut.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class SysRegisterService {

	private final ISysUserService userService;

	private final ISysLogininforService logininfoService;

	private final CaptchaService captchaService;

	/**
	 * 注册
	 */
	public void register(RegisterBody registerBody) {
		Assert.isTrue(SysRegistEnable.isEnable(), SysErrorCode.REGIST_DISABELD::exception);
		// 验证码开关
		if (SysCaptchaEnable.isEnable()) {
			validateCaptcha(registerBody.getUsername(), registerBody.getCaptcha());
		}
		SysUser user = new SysUser();
		user.setUserName(registerBody.getUsername());
		user.setPassword(registerBody.getPassword());
		user.setNickName(registerBody.getUsername());
		userService.registerUser(user);
		this.logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(),
				user.getUserName(), LoginLogType.REGIST, SuccessOrFail.SUCCESS, StringUtils.EMPTY);
	}

	/**
	 * 校验验证码
	 * 
	 * @param username 用户名
	 * @param captchaData 验证码
	 */
	public void validateCaptcha(String username, CaptchaData captchaData) {
		Assert.notNull(captchaData, SysErrorCode.CAPTCHA_ERR::exception);
		ICaptchaType captchaType = captchaService.getCaptchaType(captchaData.getType());
		boolean validated = captchaType.isTokenValidated(captchaData);
		if (!validated) {
			this.logininfoService.recordLogininfor(AdminUserType.TYPE, null, username,
					LoginLogType.REGIST, SuccessOrFail.FAIL, SysErrorCode.CAPTCHA_ERR.name());
			throw SysErrorCode.CAPTCHA_ERR.exception();
		}
	}
}
