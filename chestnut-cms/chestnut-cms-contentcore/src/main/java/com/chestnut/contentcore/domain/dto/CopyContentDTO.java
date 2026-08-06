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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.CONTENT.COPY_DTO}")
@Getter
@Setter
public class CopyContentDTO extends BaseDTO {

	/**
	 * 复制类型<br/>
	 * 
	 * @see com.chestnut.contentcore.ContentCoreConsts
	 */
	@XComment("{API.DOC.CMS.CONTENT.COPY_TYPE}")
	@NotNull
	@Min(1)
	@Max(3)
	public Integer copyType;
	
	/**
	 * 复制内容IDs
	 */
	@XComment("{API.DOC.CMS.CONTENT.CONTENT_IDS}")
	@NotEmpty
	public List<Long> contentIds;
	
	/**
	 * 目标栏目IDs
	 */
	@XComment("{API.DOC.CMS.CONTENT.CATALOG_IDS}")
	@NotEmpty
	public List<Long> catalogIds;

	/**
	 * 当前操作的来源站点ID
	 */
	@JsonIgnore
	private Long sourceSiteId;
}
