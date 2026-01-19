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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 栏目标签数据对象
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagCatalogVO extends TagBaseVO {

	@XComment("{CMS.CATALOG.ID}")
	private Long catalogId;

	@XComment("{CMS.CATALOG.SITE_ID}")
	private Long siteId;

	@XComment("{CMS.CATALOG.PARENT_ID}")
	private Long parentId;

	@XComment("{CMS.CATALOG.ANCESTORS}")
	private String ancestors;

	@XComment("{CMS.CATALOG.NAME}")
	private String name;

	@XComment("{CMS.CATALOG.LOGO}")
	private String logo;

	@XComment(value = "{CMS.CATALOG.LOGO_SRC}", deprecated = true, forRemoval = "1.6.0")
	private String logoSrc;

	@XComment("{CMS.CATALOG.ALIAS}")
	private String alias;

	@XComment("{CMS.CATALOG.DESC}")
	private String description;

	@XComment("{CMS.CATALOG.DEPT_CODE}")
	private String deptCode;

	@XComment("{CMS.CATALOG.TYPE}")
	private String catalogType;

	@XComment("{CMS.CATALOG.PATH}")
	private String path;
	
	@XComment("{CMS.CATALOG.REDIRECT_URL}")
	private String redirectUrl;

	@XComment("{CC.ENTITY.SORT}")
	private Long sortFlag;

	@XComment("{CMS.CATALOG.TREE_LEVEL}")
	private Integer treeLevel;

	@XComment("{CMS.CATALOG.CHILD_COUNT}")
	private Integer childCount;

	@XComment("{CMS.CATALOG.CONTENT_COUNT}")
	private Integer contentCount;

	@XComment("{CMS.CATALOG.SEO_KEYWORDS}")
	private String seoKeywords;

	@XComment("{CMS.CATALOG.SEO_DESC}")
	private String seoDescription;

	@XComment("{CMS.CATALOG.SEO_TITLE}")
	private String seoTitle;

	@XComment("{CMS.CATALOG.CONFIG_PROPS}")
	private Map<String, String> configProps;

	@XComment("{CMS.CATALOG.LINK}")
	private String link;

	@XComment("{CMS.CATALOG.LIST_LINK}")
	private String listLink;

	public static TagCatalogVO newInstance(CmsCatalog catalog, String publishPipeCode, boolean preview) {
		TagCatalogVO vo = new TagCatalogVO();
		BeanUtils.copyProperties(catalog, vo);
		if (StringUtils.isNotEmpty(catalog.getLogo())) {
			// 兼容历史版本
			vo.setLogoSrc(InternalUrlUtils.getActualUrl(catalog.getLogo(), publishPipeCode, preview));
		}
		return vo;
	}

	public Map<String, String> getConfigProps() {
		if (this.configProps == null) {
			this.configProps = new HashMap<>();
		}
		return configProps;
	}
}
