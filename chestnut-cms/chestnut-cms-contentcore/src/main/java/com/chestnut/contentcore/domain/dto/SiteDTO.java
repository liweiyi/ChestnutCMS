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
import java.util.Map;

import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import jakarta.validation.constraints.Pattern;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.contentcore.domain.CmsSite;

import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.SITE.SITE_DTO}")
@Getter
@Setter
public class SiteDTO extends BaseDTO {

	@XComment("{CMS.SITE.ID}")
	private Long siteId;

	@XComment("{CMS.SITE.NAME}")
	private String name;

	@XComment("{CMS.SITE.DESC}")
	private String description;

	@XComment("{CMS.SITE.LOGO}")
	private String logo;

	@XComment("{CMS.SITE.LOGO_SRC}")
	private String logoSrc;

	@XComment("{CMS.SITE.PATH}")
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "{VALID.CMS.SITE.DIR_PATTERN}")
	private String path;

	@XComment("{API.DOC.CMS.SITE.URL}")
	private String url;

	@XComment("{CMS.SITE.RESOURCE_URL}")
	private String resourceUrl;

	@XComment("{API.DOC.CMS.SITE.STATIC_SUFFIX}")
	private String staticSuffix;

	@XComment("{CMS.SITE.DEPT_CODE}")
	private String deptCode;

	@XComment("{CMS.SITE.SEO_KEYWORDS}")
	private String seoKeywords;

	@XComment("{CMS.SITE.SEO_DESC}")
	private String seoDescription;

	@XComment("{CMS.SITE.SEO_TITLE}")
	private String seoTitle;

	@XComment("{CMS.SITE.CONFIG_PROPS}")
	private Map<String, String> configProps;

	@XComment("{API.DOC.CMS.SITE.PUBLISH_PIPE_DATAS}")
	private List<PublishPipeProps> publishPipeDatas;

	private Map<String, Object> params;

	public static SiteDTO newInstance(CmsSite site) {
		SiteDTO dto = new SiteDTO();
		BeanUtils.copyProperties(site, dto);
		return dto;
	}
}
