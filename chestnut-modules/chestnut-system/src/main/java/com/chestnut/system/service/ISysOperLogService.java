package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysOperLog;

/**
 * 操作日志 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysOperLogService extends IService<SysOperLog> {
	
	/**
	 * 清空操作日志
	 */
	void cleanOperLog();

	/**
	 * 操作日志记录
	 *
	 * @param operLog 操作日志信息
	 */
	void recordOper(SysOperLog operLog);
}
