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
