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
package com.chestnut.member.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.domain.MemberLevelConfig;
import com.chestnut.member.domain.dto.LevelConfigDTO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.level.ILevelType;
import com.chestnut.member.level.LevelManager;
import com.chestnut.member.mapper.MemberLevelConfigMapper;
import com.chestnut.member.service.IMemberLevelConfigService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberLevelConfigServiceImpl extends ServiceImpl<MemberLevelConfigMapper, MemberLevelConfig>
		implements IMemberLevelConfigService, CommandLineRunner {

	private final Map<String, ILevelType> levelTypes;

	private Map<String, LevelManager> levelManagerMap = new HashMap<>();

	@Override
	public void addLevelConfig(LevelConfigDTO dto) {
		ILevelType levelType = this.getLevelType(dto.getLevelType());

		Long count = this.lambdaQuery().eq(MemberLevelConfig::getLevelType, levelType.getId())
				.eq(MemberLevelConfig::getLevel, dto.getLevel()).count();
		Assert.isTrue(count == 0,
				() -> MemberErrorCode.LEVEL_CONFIG_EXIST.exception(dto.getLevelType() + "=" + dto.getLevel()));

		MemberLevelConfig lvConfig = new MemberLevelConfig();
		lvConfig.setConfigId(IdUtils.getSnowflakeId());
		lvConfig.setLevelType(levelType.getId());
		lvConfig.setLevel(dto.getLevel());
		lvConfig.setName(dto.getName());
		lvConfig.setIcon(dto.getIcon());
		lvConfig.setNextNeedExp(dto.getNextNeedExp());
		lvConfig.setRemark(dto.getRemark());
		lvConfig.createBy(dto.getOperator().getUsername());
		this.save(lvConfig);

		this.onLevelConfigChange(levelType);
	}

	@Override
	public void updateLevelConfig(LevelConfigDTO dto) {
		ILevelType levelType = this.getLevelType(dto.getLevelType());
		MemberLevelConfig lvConfig = this.lambdaQuery().eq(MemberLevelConfig::getLevelType, levelType.getId())
				.eq(MemberLevelConfig::getLevel, dto.getLevel()).one();
		Assert.notNull(lvConfig, CommonErrorCode.DATA_NOT_FOUND::exception);

		lvConfig.setName(dto.getName());
		lvConfig.setIcon(dto.getIcon());
		lvConfig.setNextNeedExp(dto.getNextNeedExp());
		lvConfig.setRemark(dto.getRemark());
		lvConfig.updateBy(dto.getOperator().getUsername());
		this.updateById(lvConfig);
		
		this.onLevelConfigChange(levelType);
	}

	@Override
	public void deleteLevelConfig(List<Long> configIds) {
		List<MemberLevelConfig> list = this.listByIds(configIds);
		if (list.size() > 0) {
			this.removeByIds(list);

			ILevelType levelType = this.getLevelType(list.get(0).getLevelType());
			this.onLevelConfigChange(levelType);
		}
	}

	public void onLevelConfigChange(ILevelType levelType) {
		List<MemberLevelConfig> list = this.lambdaQuery().eq(MemberLevelConfig::getLevelType, levelType.getId())
				.orderByAsc(MemberLevelConfig::getLevel).list();
		LevelManager levelManager = this.getLevelManager(levelType);
		if (list.size() > 0) {
			levelManager.resetLevelConfigs(
					list.stream().collect(Collectors.toMap(MemberLevelConfig::getLevel, conf -> conf)));
		}
	}

	@Override
	public LevelManager getLevelManager(String levelType) {
		ILevelType lt = this.getLevelType(levelType);
		return this.getLevelManager(lt);
	}
	
	private LevelManager getLevelManager(ILevelType levelType) {
		LevelManager levelManager = this.levelManagerMap.get(levelType.getId());
		if (levelManager == null) {
			levelManager = new LevelManager();
			levelManager.setLevelType(levelType);
			this.levelManagerMap.put(levelType.getId(), levelManager);
		}
		return levelManager;
	}

	@Override
	public ILevelType getLevelType(String levelTypeId) {
		ILevelType lt = this.levelTypes.get(ILevelType.BEAN_PREFIX + levelTypeId);
		Assert.notNull(lt, () -> MemberErrorCode.UNSUPPORTED_LEVEL_TYPE.exception(levelTypeId));
		return lt;
	}
	
	@Override
	public Map<String, ILevelType> getLevelTypes() {
		return this.levelTypes;
	}

	@Override
	public void run(String... args) throws Exception {
		levelTypes.values().forEach(levelType -> onLevelConfigChange(levelType));
	}
}
