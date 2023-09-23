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
