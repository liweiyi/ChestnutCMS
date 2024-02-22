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
