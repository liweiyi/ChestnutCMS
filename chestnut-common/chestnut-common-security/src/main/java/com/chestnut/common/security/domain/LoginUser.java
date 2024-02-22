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
package com.chestnut.common.security.domain;

import com.chestnut.common.security.SecurityUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 登录用户身份权限
 */
@Getter
@Setter
public class LoginUser implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 用户类型
	 */
	private String userType;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 部门ID
	 */
	private Long deptId;

	/**
	 * 用户唯一标识
	 */
	private String token;

	/**
	 * 登录时间
	 */
	private Long loginTime;

	/**
	 * 过期时间
	 */
	private Long expireTime;

	/**
	 * 登录IP地址
	 */
	private String ipaddr;

	/**
	 * 登录地点
	 */
	private String loginLocation;

	/**
	 * 浏览器类型
	 */
	private String browser;

	/**
	 * 操作系统
	 */
	private String os;

	/**
	 * 权限列表
	 */
	private List<String> permissions;

	/**
	 * 用户信息
	 */
	private Object user;

	public boolean isSuperAdministrator() {
		return SecurityUtils.isSuperAdmin(userId);
	}

	public boolean hasPermission(String perm) {
		return isSuperAdministrator() || (Objects.nonNull(this.permissions) && this.permissions.contains(perm));
	}
}
