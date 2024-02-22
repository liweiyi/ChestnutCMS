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
package com.chestnut.vote.domain.dto;

import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.vote.domain.VoteSubjectItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveSubjectItemsDTO extends BaseDTO {

	/**
	 * 主题ID
	 */
	private Long subjectId;

	/**
	 * 问卷调查主题选项列表
	 */
	private List<VoteSubjectItem> itemList;
}
