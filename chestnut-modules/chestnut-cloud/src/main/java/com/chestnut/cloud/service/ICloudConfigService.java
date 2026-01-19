/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.cloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.cloud.domain.CcCloudConfig;
import com.chestnut.cloud.domain.dto.CreateCloudConfigRequest;
import com.chestnut.cloud.domain.dto.UpdateCloudConfigRequest;
import com.chestnut.common.cloud.ICloudProvider;

import java.util.List;

public interface ICloudConfigService extends IService<CcCloudConfig> {

    ICloudProvider getCloudProvider(String cloudProviderId);

    /**
	 * 获取云服务配置信息
	 */
    CcCloudConfig getCloudConfig(Long configId);
	
	/**
	 * 添加登录配置信息
	 * 
	 * @param req 登录配置数据
	 */
	void addConfig(CreateCloudConfigRequest req);

	/**
	 * 保存登录配置信息
	 * 
	 * @param req 登录配置数据
	 */
	void saveConfig(UpdateCloudConfigRequest req);
	
	/**
	 * 删除登录配置信息
	 * 
	 * @param configIds 配置ID列表
	 */
	void deleteConfigs(List<Long> configIds);
}
