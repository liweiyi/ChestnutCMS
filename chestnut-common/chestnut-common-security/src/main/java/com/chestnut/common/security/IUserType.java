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

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.session.SaSession;
import com.chestnut.common.security.domain.LoginUser;

import java.util.List;

/**
 * 用户类型接口
 */
public interface IUserType {

	String BEAN_PRFIX = "SATOKEN.USERTYPE.";

	/**
	 * 用户类型唯一标识
	 */
	String getType();

	/**
	 * 用户类型名称
	 */
	String getName();

	default boolean isLogin() {
		return SaManager.getStpLogic(this.getType()).isLogin();
	}

	default void login(Object loginId) {
		SaManager.getStpLogic(this.getType()).login(loginId);
	}

	default SaSession getTokenSession() {
		return SaManager.getStpLogic(this.getType()).getTokenSession();
	}

	default String getTokenValue() {
		return SaManager.getStpLogic(this.getType()).getTokenValue(true);
	}

	default LoginUser getLoginUser() {
		return this.getTokenSession().getModel(SaSession.USER, LoginUser.class);
	}

	default SaSession getTokenSessionByToken(String tokenValue) {
		return SaManager.getStpLogic(this.getType()).getTokenSessionByToken(tokenValue);
	}

	/**
	 * 获取用户权限列表
	 *
	 * @param loginUid
	 * @return
	 */
	default List<String> getPermissionList(Long loginUid) {
		return List.of();
	}

	/**
	 * 获取用户角色列表
	 *
	 * @param loginUid
	 * @return
	 */
	default List<String> getRoleList(Long loginUid) {
		return List.of();
	}
}
