package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.system.domain.SysLogininfor;

/**
 * 系统访问日志情况信息 服务层
 */
public interface ISysLogininforService extends IService<SysLogininfor> {

	/**
	 * 清空系统登录日志
	 */
	public void cleanLogininfor();

	/**
	 * 记录登录日志任务
	 * 
	 * @param userType
	 * @param userId
	 * @param username
	 * @param logType
	 * @param message
	 * @param args
	 * @return
	 */
	AsyncTask recordLogininfor(String userType, Object userId, String username, String logType, String status, String message, Object... args);
}
