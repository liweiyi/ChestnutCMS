/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName(MemberFavorites.TABLE_NAME)
public class MemberFavorites implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cc_member_favorites";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 会员ID
	 */
	private Long memberId;

	/**
	 * 收藏数据类型
	 */
	private String dataType;
	
	/**
	 * 收藏数据ID
	 */
	private Long dataId;

	/**
	 * 收藏时间
	 */
	private LocalDateTime createTime;
}
