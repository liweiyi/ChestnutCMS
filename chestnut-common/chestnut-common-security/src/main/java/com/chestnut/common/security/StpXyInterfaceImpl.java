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
package com.chestnut.common.security;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;

/**
 * SaToken 权限缓存数据接口
 */
@Component
@RequiredArgsConstructor
public class StpXyInterfaceImpl implements StpInterface {
	
	private final Map<String, IUserType> userTypes;

	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		IUserType userType = this.userTypes.get(IUserType.BEAN_PRFIX + loginType);
		return userType.getPermissionList(Long.parseLong(String.valueOf(loginId)));
	}

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		IUserType userType = this.userTypes.get(IUserType.BEAN_PRFIX + loginType);
		return userType.getRoleList(Long.parseLong(String.valueOf(loginId)));
	}
}
