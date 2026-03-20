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

import java.time.LocalDateTime;
import java.util.List;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.CONTENT.SET_TOP_DTO}")
@Getter
@Setter
public class SetTopContentDTO extends BaseDTO {

	/**
	 * 置顶内容IDs
	 */
	@XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}")
	@NotEmpty
	public List<Long> contentIds;
	
	/**
	 * 置顶结束时间
	 */
	@XComment("{API.DOC.CMS.CONTENT.TOP_END_TIME}")
	public LocalDateTime topEndTime;
}
