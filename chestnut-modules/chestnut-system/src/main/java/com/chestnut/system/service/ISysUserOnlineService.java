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
