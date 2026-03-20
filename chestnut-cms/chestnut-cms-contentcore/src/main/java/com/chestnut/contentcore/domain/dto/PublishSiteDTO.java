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

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.SITE.PUBLISH_SITE_DTO}")
@Getter
@Setter
public class PublishSiteDTO {

	/**
	 * 站点ID
	 */
	@XComment("{CMS.SITE.ID}")
	@LongId
	private Long siteId;
	
	/**
	 * 是否只发布首页
	 */
	@XComment("{API.DOC.CMS.SITE.PUBLISH_INDEX}")
	@NotNull
	private boolean publishIndex;
	
	/**
	 * 发布内容状态
	 */
	@XComment("{API.DOC.CMS.SITE.CONTENT_STATUS}")
	private String contentStatus;
}
