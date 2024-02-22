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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishCatalogDTO {

	/**
	 * 栏目ID
	 */
	@LongId
	private Long catalogId;
	
	/**
	 * 是否发布子栏目
	 */
	@NotNull
	private Boolean publishChild;
	
	/**
	 * 是否发布栏目内容
	 */
	@NotNull
	private Boolean publishDetail;
	
	/**
	 * 发布内容状态
	 */
	@NotEmpty
	private String publishStatus;
}
