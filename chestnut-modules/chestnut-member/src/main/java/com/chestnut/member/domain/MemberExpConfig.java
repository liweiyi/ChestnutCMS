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
 * 会员经验配置表
 */
@Getter
@Setter
@TableName(MemberExpConfig.TABLE_NAME)
public class MemberExpConfig extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_exp_config";

	@TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;
	
	/**
	 * 操作项ID
	 */
	private String opType;
	
	/**
	 * 操作项名称
	 */
	@TableField(exist = false)
	private String opTypeName;
	
	/**
	 * 积分类型
	 */
	private String levelType;
	
	/**
	 * 积分类型名称
	 */
	@TableField(exist = false)
	private String levelTypeName;
	
	/**
	 * 积分变更值
	 */
	private Integer exp;
	
	/**
	 * 每日生效次数上限
	 */
	private Integer dayLimit;
	
	/**
	 * 总生效次数上限
	 */
	private Integer totalLimit;
}
