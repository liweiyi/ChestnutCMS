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
package com.chestnut.contentcore.domain.dto;

import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyContentDTO extends BaseDTO {

	/**
	 * 复制类型<br/>
	 * 
	 * @see com.chestnut.contentcore.ContentCoreConsts
	 */
	@NotNull
	public Integer copyType;
	
	/**
	 * 复制内容IDs
	 */
	@NotEmpty
	public List<Long> contentIds;
	
	/**
	 * 目标栏目IDs
	 */
	@NotEmpty
	public List<Long> catalogIds;
}
