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
