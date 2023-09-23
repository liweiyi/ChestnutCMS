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
