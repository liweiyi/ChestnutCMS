/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.dto.CreateLoginConfigRequest;
import com.chestnut.system.domain.dto.UpdateLoginConfigRequest;
import com.chestnut.system.security.ILoginType;

import java.util.List;

public interface ILoginConfigService extends IService<SysLoginConfig> {

    ILoginType getLoginType(String type);

    /**
	 * 获取登录配置信息
	 */
    SysLoginConfig getLoginConfig(Long configId);
	
	/**
	 * 添加登录配置信息
	 * 
	 * @param req 登录配置数据
	 */
	void addConfig(CreateLoginConfigRequest req);

	/**
	 * 保存登录配置信息
	 * 
	 * @param req 登录配置数据
	 */
	void saveConfig(UpdateLoginConfigRequest req);
	
	/**
	 * 删除登录配置信息
	 * 
	 * @param configIds 配置ID列表
	 */
	void deleteConfigs(List<Long> configIds);
}
