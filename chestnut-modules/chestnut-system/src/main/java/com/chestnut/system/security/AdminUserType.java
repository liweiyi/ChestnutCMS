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
package com.chestnut.system.security;

import cn.dev33.satoken.session.SaSession;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.IUserType;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component(IUserType.BEAN_PRFIX + AdminUserType.TYPE)
public class AdminUserType implements IUserType {

	public static final String TYPE = "sys_user";

	private final ISysRoleService roleService;

	static {
		// 调用一次StpAdminUtil中的方法，保证其可以尽早的初始化 StpLogic ,@see SaManager.setConfig
		StpAdminUtil.getLoginType();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return I18nUtils.get("{SATOKEN.USERTYPE.ADMIN}");
	}

	@Override
	public List<String> getPermissionList(Long loginUid) {
		SaSession tokenSession = StpAdminUtil.getTokenSessionByToken(StpAdminUtil.getTokenValueByLoginId(loginUid));
		return tokenSession.getModel(SaSession.USER, LoginUser.class).getPermissions();
	}

	@Override
	public List<String> getRoleList(Long loginUid) {
		return roleService.selectRoleKeysByUserId(loginUid);
	}
}
