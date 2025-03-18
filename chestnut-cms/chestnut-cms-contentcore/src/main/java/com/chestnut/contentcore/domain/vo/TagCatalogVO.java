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

	@XComment("栏目ID")
	private Long catalogId;

	@XComment("站点ID")
	private Long siteId;

	@XComment("父级栏目ID")
	private Long parentId;

	@XComment("祖级栏目IDs")
	private String ancestors;

	@XComment("栏目名称")
	private String name;

	@XComment("栏目引导图")
	private String logo;

	@XComment(value = "栏目引导图访问路径", deprecated = true, forRemoval = "1.6.0")
	private String logoSrc;

	@XComment("栏目别名")
	private String alias;

	@XComment("栏目简介")
	private String description;

	@XComment("所属部门编码")
	private String deptCode;

	@XComment("栏目类型")
	private String catalogType;

	@XComment("栏目目录")
	private String path;
	
	@XComment("标题栏目跳转地址")
	private String redirectUrl;

	@XComment("排序值")
	private Long sortFlag;

	@XComment("栏目层级")
	private Integer treeLevel;

	@XComment("子栏目数")
	private Integer childCount;

	@XComment("内容数量")
	private Integer contentCount;

	@XComment("SEO关键词")
	private String seoKeywords;

	@XComment("SEO描述")
	private String seoDescription;

	@XComment("SEO标题")
	private String seoTitle;

	@XComment("扩展配置")
	private Map<String, String> configProps;

	@XComment("栏目链接")
	private String link;

	@XComment("列表页链接（无首页模板时与link一致）")
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
