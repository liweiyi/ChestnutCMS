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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 问卷调查主题选项表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = VoteSubjectItem.TABLE_NAME)
public class VoteSubjectItem extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_item";

	@TableId(value = "item_id", type = IdType.INPUT)
	private Long itemId;

	/**
	 * 关联问卷调查表ID
	 */
	private Long voteId;

	/**
	 * 关联主题ID
	 */
	private Long subjectId;
	
	/**
	 * 类型（文字、图片、内容引用）
	 */
	private String type;
	
	/**
	 * 选项内容（文字内容、图片地址、内容引用地址）
	 */
	private String content;
	
	/**
	 * 选项描述
	 */
	private String description;

	/**
	 * 排序标识
	 */
	private Integer sortFlag;
	
	/**
	 * 选项票数
	 */
	private Integer total;
	
	/**
	 * 总票数
	 */
	@TableField(exist = false)
	private Integer voteTotal;
}
