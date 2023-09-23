package com.chestnut.system.service;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysUserOnline;

/**
 * 在线用户 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysUserOnlineService {
	
	/**
	 * 设置在线用户信息
	 * 
	 * @param user
	 *            用户信息
	 * @return 在线用户
	 */
	public SysUserOnline loginUserToUserOnline(LoginUser user);
}
