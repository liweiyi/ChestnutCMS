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
package com.chestnut.member.fixed.config;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;

@Component(FixedConfig.BEAN_PREFIX + EncryptMemberPhoneAndEmail.ID)
public class EncryptMemberPhoneAndEmail extends FixedConfig {

	public static final String ID = "EncryptMemberPhoneAndEmail";
	
	private static final String DEFAULT_VALUE = "true";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	public EncryptMemberPhoneAndEmail() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
	
	public static boolean getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isEmpty(configValue)) {
			return true;
		}
		return Boolean.parseBoolean(configValue);
	}
	
	public static String encryptEmail(String email) {
		if (StringUtils.isEmpty(email)) {
			return email;
		}
		String prefix = StringUtils.substringBeforeLast(email, "@");
		prefix = prefix.substring(0, prefix.length() / 2);
		return prefix + "*" + email.substring(email.indexOf("@"));
	}
	
	public static String encryptPhone(String phone) {
		if (StringUtils.isEmpty(phone) || phone.length() != 11) {
			return phone;
		}
		return phone.replaceAll("(?<=\\d{3})\\d(?=\\d{3})", "*");
	}
}
