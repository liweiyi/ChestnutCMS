package com.chestnut.system.service.impl;

import com.chestnut.common.async.AsyncTaskManager;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.system.domain.SysOperLog;
import com.chestnut.system.mapper.SysOperLogMapper;
import com.chestnut.system.service.ISysOperLogService;

import lombok.RequiredArgsConstructor;

/**
 * 操作日志 服务层处理
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

	private final SysOperLogMapper operLogMapper;

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 清空操作日志
	 */
	@Override
	public void cleanOperLog() {
		operLogMapper.cleanOperLog();
	}

	/**
	 * 操作日志记录
	 *
	 * @param operLog 操作日志信息
	 */
	@Override
	public void recordOper(final SysOperLog operLog) {
		asyncTaskManager.execute(() -> {
			operLog.setOperLocation(IP2RegionUtils.ip2Region(operLog.getOperIp()));
			save(operLog);
		});
	}
}
