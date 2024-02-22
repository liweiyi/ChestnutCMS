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
import com.chestnut.system.domain.SysConfig;

/**
 * 参数配置 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysConfigService extends IService<SysConfig> {

	/**
	 * 根据键名查询参数配置信息
	 * 
	 * @param configKey
	 *            参数键名
	 * @return 参数键值
	 */
	public String selectConfigByKey(String configKey);

	/**
	 * 新增参数配置
	 * 
	 * @param config
	 *            参数配置信息
	 * @return 结果
	 */
	public void insertConfig(SysConfig config);

	/**
	 * 修改参数配置
	 * 
	 * @param config
	 *            参数配置信息
	 * @return 结果
	 */
	public void updateConfig(SysConfig config);

	/**
	 * 批量删除参数信息
	 * 
	 * @param configIds
	 *            需要删除的参数ID
	 */
	public void deleteConfigByIds(List<Long> configIds);

	/**
	 * 加载参数缓存数据
	 */
	public void loadingConfigCache();

	/**
	 * 清空参数缓存数据
	 */
	public void clearConfigCache();

	/**
	 * 重置参数缓存数据
	 */
	public void resetConfigCache();
}
