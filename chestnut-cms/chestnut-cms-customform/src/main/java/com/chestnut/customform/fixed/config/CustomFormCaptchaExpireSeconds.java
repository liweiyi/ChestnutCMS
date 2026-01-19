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
package com.chestnut.customform.fixed.config;

import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

/**
 * 自定义表单验证码过期时间（秒）
 */
@Component(FixedConfig.BEAN_PREFIX + CustomFormCaptchaExpireSeconds.ID)
public class CustomFormCaptchaExpireSeconds extends FixedConfig {

	public static final String ID = "CustomFormCaptchaExpireSeconds";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	/**
	 * 默认：600秒
	 */
	private static final int DEFAULT_VALUE = 600;

	public CustomFormCaptchaExpireSeconds() {
		super(ID, "{CONFIG." + ID + "}", String.valueOf(DEFAULT_VALUE), null);
	}
	
	public static Integer getSeconds() {
		String configValue = configService.selectConfigByKey(ID);
		return ConvertUtils.toInteger(configValue, DEFAULT_VALUE);
	}
}
