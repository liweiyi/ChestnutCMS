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
