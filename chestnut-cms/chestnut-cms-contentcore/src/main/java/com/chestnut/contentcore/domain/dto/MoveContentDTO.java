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
package com.chestnut.contentcore.domain.dto;

import java.util.List;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.CONTENT.MOVE_DTO}")
@Getter
@Setter
public class MoveContentDTO extends BaseDTO {

	/**
	 * 转移内容IDs
	 */
	@XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}")
	@NotEmpty
	public List<Long> contentIds;
	
	/**
	 * 目标栏目ID
	 */
	@XComment("{API.DOC.CMS.CONTENT.TO_CATALOG_ID}")
	@LongId
	public Long catalogId;
}
