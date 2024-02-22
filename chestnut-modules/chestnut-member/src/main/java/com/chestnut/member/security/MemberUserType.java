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
package com.chestnut.member.security;

import org.springframework.stereotype.Component;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.IUserType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IUserType.BEAN_PRFIX + MemberUserType.TYPE)
public class MemberUserType implements IUserType {

	public static final String TYPE = "cc_member";

	static {
		// 调用一次StpMemberUtil中的方法，保证其可以尽早的初始化 StpLogic ,@see SaManager.setConfig
		StpMemberUtil.getLoginType();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return I18nUtils.get("{SATOKEN.USERTYPE.MEMBER}");
	}
}
