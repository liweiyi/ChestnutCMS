package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysLogininfor;

/**
 * 系统访问日志情况信息 服务层
 */
public interface ISysLogininforService extends IService<SysLogininfor> {

	/**
	 * 清空系统登录日志
	 */
	void cleanLogininfor();

	/**
	 * 记录登录日志任务
	 *
	 * @param userType 用户类型
	 * @param userId 用户ID
	 * @param username 用户名
	 * @param logType 日志类型
	 * @param message 日志详情
	 * @param args 参数
	 */
	void recordLogininfor(String userType, Object userId, String username, String logType, String status, String message, Object... args);
}
