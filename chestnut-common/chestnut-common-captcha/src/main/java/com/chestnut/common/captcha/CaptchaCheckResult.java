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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 验证码数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
public class CaptchaCheckResult {

	/**
	 * 是否校验成功
	 */
	private Boolean success;

	/**
	 * 结果自定义数据
	 */
	private String data;

	public CaptchaCheckResult(boolean success) {
		this.success = success;
	}

	public CaptchaCheckResult(boolean success, String data) {
		this.success = success;
		this.data = data;
	}

	public static CaptchaCheckResult success() {
		return new CaptchaCheckResult(true);
	}

	public static CaptchaCheckResult success(String data) {
		return new CaptchaCheckResult(true, data);
	}

	public static CaptchaCheckResult fail() {
		return new CaptchaCheckResult(false);
	}
}