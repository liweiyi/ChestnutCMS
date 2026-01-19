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
package com.chestnut.common.captcha.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码通用配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.captcha")
public class CaptchaProperties {

	/**
	 * 验证码过期时间，单位：秒，默认：10分钟
	 */
	private Long expireSeconds = 600L;
}
