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

import cn.dev33.satoken.secure.BCrypt;

/**
 * 安全服务工具类
 */
public class SecurityUtils {
	
	private static final long SUPER_ADMIN_UID = 1;
	
	/**
	 * 是否超级管理员
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean isSuperAdmin(long userId) {
		return userId == SUPER_ADMIN_UID;
	}
	
	/**
	 * 密码加密
	 * 
	 * @param password
	 * @return
	 */
	public static String passwordEncode(String password) {
		return BCrypt.hashpw(password);
	}

	/**
	 * 校验密码
	 * 
	 * @param rawPassword
	 * @param encodedPassword
	 * @return
	 */
	public static boolean matches(String password, String encodedPassword) {
		return BCrypt.checkpw(password, encodedPassword);
	}
}
