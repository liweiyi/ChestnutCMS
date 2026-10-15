/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName(MemberLike.TABLE_NAME)
@XComment("{API.DOC.MEMBER.INTERACTION_LIKE_RECORD}")
public class MemberLike implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cc_member_like";

	@TableId(value = "log_id", type = IdType.AUTO)
	@XComment("{API.DOC.MEMBER.INTERACTION_LOG_ID}")
	private Long logId;

	/**
	 * 会员ID
	 */
	@XComment("{API.DOC.MEMBER.MEMBER_ID}")
	private Long memberId;

	/**
	 * 点赞数据类型
	 */
	@XComment("{API.DOC.MEMBER.DATA_TYPE}")
	private String dataType;
	
	/**
	 * 点赞数据ID
	 */
	@XComment("{API.DOC.MEMBER.DATA_ID}")
	private Long dataId;

	/**
	 * 点赞时间
	 */
	@XComment("{API.DOC.MEMBER.INTERACTION_LIKE_TIME}")
	private LocalDateTime createTime;
}
