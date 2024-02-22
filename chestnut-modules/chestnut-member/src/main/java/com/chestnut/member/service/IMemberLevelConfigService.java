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
package com.chestnut.member.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberLevelConfig;
import com.chestnut.member.domain.dto.LevelConfigDTO;
import com.chestnut.member.level.ILevelType;
import com.chestnut.member.level.LevelManager;

/**
 * 会员等级配置 服务类
 * 
 */
public interface IMemberLevelConfigService extends IService<MemberLevelConfig> {
	
	/**
	 * 新增等级配置
	 * 
	 * @param dto
	 */
	void addLevelConfig(LevelConfigDTO dto);
	
	/**
	 * 更新等级配置
	 * 
	 * @param dto
	 */
	void updateLevelConfig(LevelConfigDTO dto);
	
	/**
	 * 删除等级配置
	 * 
	 * @param configIds
	 */
	void deleteLevelConfig(List<Long> configIds);

	/**
	 * 获取指定等级类型的等级管理器
	 * 
	 * @param levelType
	 * @return
	 */
	LevelManager getLevelManager(String levelType);

	/**
	 * 获取等级类型实例
	 * 
	 * @param levelTypeId
	 * @return
	 */
	ILevelType getLevelType(String levelTypeId);

	/**
	 * 获取等级类型集合
	 * 
	 * @return
	 */
	Map<String, ILevelType> getLevelTypes();
}
