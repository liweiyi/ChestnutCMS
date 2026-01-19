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
package com.chestnut.common.captcha;

import com.chestnut.common.exception.ErrorCode;

/**
 * 验证码错误码
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CaptchaErrorCode implements ErrorCode {

	/**
	 * 不支持的验证码类型：{0}
	 */
	UNSUPPORTED_CAPTCHA_TYPE,

	/**
	 * 生成验证码失败：{0}
	 */
	GENERATE_CAPTCHA_FAILED,

	/**
	 * 验证码已失效，请重新获取
	 */
	INVALID_CAPTCHA,

    /**
     * 验证码标识不能为空
     */
    CAPTCHA_TOKEN_NOT_EMPTY,

    /**
     * 验证码请求繁忙，请稍后再试。
     */
    CAPTCHA_LIMIT,

    /**
     * 手机号码格式错误
     */
    INVALID_PHONE_NUMBER,

    INVALID_SMS_CONFIG_ID;

	@Override
	public String value() {
		return "{ERR.CAPTCHA." + this.name() + "}";
	}
}
