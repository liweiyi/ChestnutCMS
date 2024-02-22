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
package com.chestnut.vote.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteVO {

	/**
	 * ID
	 */
	private Long voteId;
	
	/**
	 * 问卷调查标题
	 */
	private String title;

	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	private LocalDateTime endTime;
	
	/**
	 * 参与人类型
	 */
	private String userType;
	
	/**
	 * 每日限制次数
	 */
	private Integer dayLimit;
	
	/**
	 * 总共可参与次数
	 */
	private Integer totalLimit;
	
	/**
	 * 结果查看方式（不允许查看、提交后可看、不限制）
	 */
	private String viewType;
	
	/**
	 * 已参与人数
	 */
	private Integer total;
	
	/**
	 * 主题列表
	 */
	private List<VoteSubjectVO> subjects;
}
