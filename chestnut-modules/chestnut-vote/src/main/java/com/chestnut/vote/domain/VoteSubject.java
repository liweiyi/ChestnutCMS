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

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 问卷调查主题表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = VoteSubject.TABLE_NAME)
public class VoteSubject extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_subject";

	@TableId(value = "subject_id", type = IdType.INPUT)
	private Long subjectId;

	/**
	 * 关联问卷调查表ID
	 */
	private Long voteId;
	
	/**
	 * 类型（单选、多选、输入）
	 */
	private String type;
	
	/**
	 * 标题
	 */
	private String title;

	/**
	 * 排序标识
	 */
	private Integer sortFlag;
	
	/**
	 * 选项列表
	 */
	@TableField(exist = false)
	private List<VoteSubjectItem> itemList;
	
	/**
	 * 排到指定主题前
	 */
	@TableField(exist = false)
	private Long nextSubjectId;
}
