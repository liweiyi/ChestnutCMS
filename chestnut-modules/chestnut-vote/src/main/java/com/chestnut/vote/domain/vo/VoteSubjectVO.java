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
package com.chestnut.vote.domain.vo;

import java.util.List;

import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteSubjectVO {

	/**
	 * ID
	 */
	@XComment("{CC.VOTE_SUBJECT.ID}")
	private Long subjectId;
	
	/**
	 * 类型（单选、多选、输入）
	 */
	@XComment("{CC.VOTE_SUBJECT.TYPE}")
	private String type;
	
	/**
	 * 标题
	 */
	@XComment("{CC.VOTE_SUBJECT.TITLE}")
	private String title;

	/**
	 * 排序标识
	 */
	@XComment("{CC.ENTITY.SORT}")
	private Integer sortFlag;

	/**
	 * 选项列表
	 */
	@XComment("{CC.VOTE_SUBJECT.ITEM_LIST}")
	private List<VoteSubjectItemVO> items;
}
