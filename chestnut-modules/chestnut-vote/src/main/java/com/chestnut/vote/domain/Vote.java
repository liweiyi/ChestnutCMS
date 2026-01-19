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
package com.chestnut.vote.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.system.validator.Dict;
import com.chestnut.vote.fixed.dict.VoteStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

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

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote";

	@XComment("{CC.VOTE.ID}")
	@TableId(value = "vote_id", type = IdType.INPUT)
	private Long voteId;

	/**
	 * 唯一标识编码
	 */
	@XComment("{CC.VOTE.CODE}")
	@Pattern(regexp = RegexConsts.REGEX_CODE)
	private String code;

	/**
	 * 问卷调查标题
	 */
	@NotEmpty
	@XComment("{CC.VOTE.TITLE}")
	private String title;

	/**
	 * 开始时间
	 */
	@NotNull
	@XComment("{CC.VOTE.START_TIME}")
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	@NotNull
	@XComment("{CC.VOTE.END_TIME}")
	private LocalDateTime endTime;

	/**
	 * 用户类型（IP、浏览器指纹，登录用户）
	 */
	@NotEmpty
	@XComment("{CC.VOTE.USER_TYPE}")
	private String userType;

	/**
	 * 每日限制次数
	 */
	@Min(1)
	@XComment("{CC.VOTE.DAY_LIMIT}")
	private Integer dayLimit;

	/**
	 * 总共可参与次数
	 */
	@Min(1)
	@XComment("{CC.VOTE.TOTAL_LIMIT}")
	private Integer totalLimit;

	/**
	 * 状态
	 * 
	 * @see com.chestnut.vote.fixed.dict.VoteStatus
	 */
	@Dict(VoteStatus.TYPE)
	@XComment("{CC.VOTE.STATUS}")
	private String status;

	/**
	 * 结果查看方式（不允许查看、提交后可看、不限制）
	 */
	@NotEmpty
	@XComment("{CC.VOTE.VIEW_TYPE}")
	private String viewType;

	/**
	 * 提交总人数
	 */
	@XComment("{CC.VOTE.TOTAL}")
	private Integer total;
	
	/**
	 * 来源归属标识，例如：cms:siteid
	 */
	@XComment("{CC.VOTE.SOURCE}")
	private String source;

	/**
	 * 主题列表
	 */
	@XComment("{CC.VOTE.SUBJECT_LIST}")
	@TableField(exist = false)
	private List<VoteSubject> subjectList;
}
