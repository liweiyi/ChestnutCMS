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
import java.util.Map;

import jakarta.validation.constraints.Pattern;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.contentcore.domain.CmsSite;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteDTO extends BaseDTO {

	private Long siteId;

	private String name;

	private String description;

	private String logo;

	private String logoSrc;

	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "站点目录只能使用大小写字母及数字组合")
	private String path;

	private String url;

	private String resourceUrl;

	private String staticSuffix;

	private String deptCode;

	private String seoKeywords;

	private String seoDescription;

	private String seoTitle;

	private Map<String, String> configProps;

	private List<PublishPipeProp> publishPipeDatas;

	private Map<String, Object> params;

	public static SiteDTO newInstance(CmsSite site) {
		SiteDTO dto = new SiteDTO();
		BeanUtils.copyProperties(site, dto);
		return dto;
	}
}
