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

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.system.validator.Dict;
import com.chestnut.vote.fixed.dict.VoteStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 问卷调查表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = Vote.TABLE_NAME)
public class Vote extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote";

	@TableId(value = "vote_id", type = IdType.INPUT)
	private Long voteId;

	/**
	 * 唯一标识编码
	 */
	@Pattern(regexp = "^[A-Za-z0-9_]+$", message = "编码不能为空且只能使用大小写字母及数字组合")
	private String code;

	/**
	 * 问卷调查标题
	 */
	@NotEmpty
	private String title;

	/**
	 * 开始时间
	 */
	@NotNull
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	@NotNull
	private LocalDateTime endTime;

	/**
	 * 用户类型（IP、浏览器指纹，登录用户）
	 */
	@NotEmpty
	private String userType;

	/**
	 * 每日限制次数
	 */
	@Min(1)
	private Integer dayLimit;

	/**
	 * 总共可参与次数
	 */
	@Min(1)
	private Integer totalLimit;

	/**
	 * 状态
	 * 
	 * @see com.chestnut.vote.fixed.dict.VoteStatus
	 */
	@Dict(VoteStatus.TYPE)
	private String status;

	/**
	 * 结果查看方式（不允许查看、提交后可看、不限制）
	 */
	@NotEmpty
	private String viewType;

	/**
	 * 提交总人数
	 */
	private Integer total;
	
	/**
	 * 来源归属标识，例如：cms:siteid
	 */
	private String source;

	/**
	 * 主题列表
	 */
	@TableField(exist = false)
	private List<VoteSubject> subjectList;
}
