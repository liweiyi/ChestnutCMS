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

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.security.ISecurityUser;

public interface ISecurityConfigService extends IService<SysSecurityConfig> {
	
	/**
	 * 获取安全配置信息
	 * 
	 * @return
	 */
	SysSecurityConfig getSecurityConfig();
	
	/**
	 * 添加安全配置信息
	 * 
	 * @param config
	 * @return
	 */
	void addConfig(SysSecurityConfig config);

	/**
	 * 保存安全配置信息
	 * 
	 * @param config
	 * @return
	 */
	void saveConfig(SysSecurityConfig config);
	
	/**
	 * 删除安全配置信息
	 * 
	 * @param configIds
	 * @param operator
	 */
	void deleteConfigs(List<Long> configIds);
	
	/**
	 * 密码错误处理，如果触发了安全策略返回false。
	 * 
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean processLoginPasswordError(ISecurityUser user);

	/**
	 * 登录成功处理
	 * 
	 * @param user
	 */
	void onLoginSuccess(ISecurityUser user);

	/**
	 * 校验密码
	 *
	 * @param user
	 * @param password
	 * @return
	 */
	void validPassword(ISecurityUser user, String password);

	/**
	 * 重置密码后强制下次登录修改密码
	 * 
	 * @param user
	 */
	void forceModifyPwdAfterResetPwd(ISecurityUser user);

	/**
	 * 后台添加 用户后强制下次登录修改密码
	 * 
	 * @param user
	 */
	void forceModifyPwdAfterUserAdd(ISecurityUser user);

	/**
	 * 启用指定安全配置
	 * 
	 * @param configId
	 */
	void changeConfigStatus(Long configId);
}
