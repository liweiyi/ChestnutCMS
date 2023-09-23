package com.chestnut.member.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员等级配置DTO
 */
@Getter
@Setter
public class LevelConfigDTO extends BaseDTO {
	
	/**
	 * 等级类型
	 */
	private String levelType;

	/**
	 * 显示级别
	 */
	private Integer level;
	
	/**
	 * 等级名称
	 */
	private String name;
	
	/**
	 * 等级图标
	 */
	private String icon;
	
	/**
	 * 升级到下一级所需经验值
	 */
	private Long nextNeedExp;
	
	/**
	 * 备注
	 */
	private String remark;
}
