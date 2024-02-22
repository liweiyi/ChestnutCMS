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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteSubjectItemVO {

	/**
	 * ID
	 */
	private Long itemId;
	
	/**
	 * 主题ID
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
	 * 票数
	 */
	private Long voteTotal;
}
