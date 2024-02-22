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

import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogApplyPublishPipeDTO extends BaseDTO {

	/*
	 * 源栏目ID
	 */
	@LongId
	public Long catalogId;

	/*
	 * 发布通道编码，应用发布通道属性值时必填项
	 */
	public String publishPipeCode;

	/*
	 * 应用发布通道属性Key列表
	 */
	@NotEmpty
	public List<String> publishPipePropKeys;
}
