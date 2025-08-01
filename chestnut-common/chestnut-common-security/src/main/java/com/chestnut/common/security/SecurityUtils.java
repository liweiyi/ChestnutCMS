/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.common.security.config.properties.SecurityProperties;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

	/**
	 * 超级管理员固定UID
	 */
	private static final long SUPER_ADMIN_UID = 1;

	/*
	 * 共有三种类型可选（ARGON2i,ARGON2d,ARGON2id;）
	 * Argon2d：专注于防御 GPU 并行攻击，使用数据依赖访问，适合需要极高抗 GPU 能力的场景。
	 * Argon2i：主要防御侧信道攻击（如时间攻击），通过随机访问内存来保护敏感信息。
	 * Argon2id：结合了 Argon2d 和 Argon2i 的优势，推荐作为通用场景的首选哈希算法。
	 */
	public static final Argon2Factory.Argon2Types TYPE = Argon2Factory.Argon2Types.ARGON2id;

	private static final Argon2 INSTANCE = Argon2Factory.create(TYPE);

	private static SecurityProperties SECURITY_PROPERTIES;

	public static void setSecurityProperties(SecurityProperties securityProperties) {
		SECURITY_PROPERTIES = securityProperties;
	}

	/**
	 * 是否超级管理员
	 * 
	 * @param userId 用户UID
	 * @return 结果
	 */
	public static boolean isSuperAdmin(long userId) {
		return userId == SUPER_ADMIN_UID;
	}
	
	/**
	 * 密码加密
	 * 
	 * @param password 明文密码
	 * @return 密文
	 */
	public static String passwordEncode(String password) {
		return INSTANCE.hash(SECURITY_PROPERTIES.getArgon2().getIterations(), SECURITY_PROPERTIES.getArgon2().getMemory(),
				SECURITY_PROPERTIES.getArgon2().getParallelism(), password.toCharArray());
	}

	/**
	 * 校验密码
	 * 
	 * @param password 明文密码
	 * @param encodedPassword 密文
	 * @return 对比结果
	 */
	public static boolean matches(String password, String encodedPassword) {
		if (encodedPassword.startsWith("$argon")) {
			return INSTANCE.verify(encodedPassword, password.toCharArray());
		}
		// TODO remove for 1.6.0
		return BCrypt.checkpw(password, encodedPassword);
	}
}
