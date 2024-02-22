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
package com.chestnut.vote.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Getter;
import lombok.Setter;

/**
 * 问卷调查日志表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = VoteLog.TABLE_NAME, autoResultMap = true)
public class VoteLog implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 关联问卷调查表ID
	 */
	private Long voteId;
	
	/**
	 * 参与人类型
	 */
	private String userType;
	
	/**
	 * 参与人ID
	 */
	private String userId;

	/**
	 * 投票结果
	 * 
	 * 格式：<subjectId, itemId|inputText>
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<Long, String> result;
	
	/**
	 * 日志记录时间
	 */
	private LocalDateTime logTime;
	
	/**
	 * IP
	 */
	private String ip;
	
	/**
	 * UserAgent
	 */
	private String userAgent;
}
