package com.chestnut.system.security;

import java.time.LocalDateTime;

/**
 * 安全配置应用对象接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISecurityUser {

	/**
	 * 用户类型
	 */
	String getType();

	/**
	 * 用户ID
	 */
	Long getUserId();

	/**
	 * 用户名
	 */
	String getUserName();

	/**
	 * 手机号
	 */
	String getPhoneNumber();

	/**
	 * Email
	 */
	String getEmail();

	/**
	 * 昵称
	 */
	String getNickName();

	/**
	 * 真实姓名
	 */
	String getRealName();

	/**
	 * 出生日期
	 */
	LocalDateTime getBirthday();

	/**
	 * 封禁用户
	 */
	void disableUser();

	/**
	 * 锁定用户
	 * 
	 * @param lockEndTime
	 */
	void lockUser(LocalDateTime lockEndTime);

	/**
	 * 强制登录后修改密码
	 */
	void forceModifyPassword();
}
