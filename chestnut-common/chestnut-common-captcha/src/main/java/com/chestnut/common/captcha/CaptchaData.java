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

import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class CaptchaData {

	/**
	 * 验证码类型
	 */
	private String type;

	/**
	 * 验证码唯一标识
	 */
	private String token;

	/**
	 * 验证码校验数据，自定义格式
	 */
	private String data;

	/**
	 * 验证码配置
	 */
	private ObjectNode properties;

	public CaptchaData(String type) {
		this.type = type;
	}

	public CaptchaData(String type, String token) {
		this.type = type;
		this.token = token;
	}
}