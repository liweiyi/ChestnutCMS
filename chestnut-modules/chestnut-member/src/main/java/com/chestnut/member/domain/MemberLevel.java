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

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员等级经验数据表
 */
@Getter
@Setter
@TableName(MemberLevel.TABLE_NAME)
public class MemberLevel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_level";

	@TableId(value = "data_id", type = IdType.INPUT)
    private Long dataId;
	
	/**
	 * 会员ID
	 */
	private Long memberId;
	
	/**
	 * 等级类型
	 */
	private String levelType;
	
	/**
	 * 当前等级
	 */
	private Integer level;
	
	/**
	 * 当前经验值
	 */
	private Long exp;
}
