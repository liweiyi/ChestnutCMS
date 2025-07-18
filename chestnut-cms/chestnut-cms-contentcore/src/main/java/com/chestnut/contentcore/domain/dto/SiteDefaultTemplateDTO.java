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
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SiteDefaultTemplateDTO extends BaseDTO {

	/*
	 * 站点ID
	 */
	@LongId
	public Long siteId;

	/*
	 * 已用到指定栏目IDs
	 */
	public List<Long> toCatalogIds;

	/*
	 * 默认模板属性
	 */
	@NotEmpty(message = "{VALIDATOR.CMS.SITE.PUBLISH_PIPE_PROPS_EMPTY}")
	public List<PublishPipeProps> publishPipeProps;
}
