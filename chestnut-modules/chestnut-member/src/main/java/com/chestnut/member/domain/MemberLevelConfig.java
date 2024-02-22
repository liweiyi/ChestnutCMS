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
package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员等级配置表
 */
@Getter
@Setter
@TableName(MemberLevelConfig.TABLE_NAME)
public class MemberLevelConfig extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_level_config";

	@TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;
	
	/**
	 * 等级类型
	 */
	private String levelType;
	
	/**
	 * 积分类型名称
	 */
	@TableField(exist = false)
	private String levelTypeName;
	
	/**
	 * 显示级别
	 */
	private Integer level;
	
	/**
	 * 显示名称
	 */
	private String name;
	
	/**
	 * 显示图标
	 */
	private String icon;
	
	/**
	 * 升级到下一级所需要的经验值
	 */
	private Long nextNeedExp;
}
