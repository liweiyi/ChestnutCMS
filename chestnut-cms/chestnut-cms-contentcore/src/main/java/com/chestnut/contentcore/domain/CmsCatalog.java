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
package com.chestnut.contentcore.domain;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.contentcore.core.impl.PublishPipeProp_IndexTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_ListTemplate;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.YesOrNo;

import lombok.Getter;
import lombok.Setter;

/**
 * 栏目表对象 [cms_catalog]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CmsCatalog.TABLE_NAME, autoResultMap = true)
public class CmsCatalog extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cms_catalog";
	
	/**
	 * 栏目ID
	 */
	@TableId(value = "catalog_id", type = IdType.INPUT)
	private Long catalogId;

	/**
	 * 站点ID
	 */
	private Long siteId;

	/**
	 * 父级栏目ID
	 */
	private Long parentId;

	/**
	 * 祖级栏目ID列表
	 */
	private String ancestors;

	/**
	 * 栏目名称
	 */
	private String name;

	/**
	 * 父级栏目名称（非表字段）
	 */
	@TableField(exist = false)
	private String parentName;

	/**
	 * logo
	 */
	private String logo;

	/**
	 * logo预览地址（非表字段）
	 */
	@TableField(exist = false)
	private String logoSrc;

	/**
	 * 栏目别名
	 */
	private String alias;

	/**
	 * 栏目简介
	 */
	private String description;

	/**
	 * 所属部门编码
	 */
	private String deptCode;

	/**
	 * 栏目类型
	 */
	private String catalogType;

	/**
	 * 栏目目录
	 */
	private String path;
	
	/**
	 * 跳转地址，标题栏目跳转地址
	 */
	private String redirectUrl;

	/**
	 * 是否生成静态页面
	 */
	private String staticFlag;

	/**
	 * 栏目是否可见
	 */
	private String visibleFlag;

	/**
	 * 排序字段
	 */
	private Long sortFlag;

	/**
	 * 栏目首页命名
	 */
	private String indexFileName;

	/**
	 * 列表页命名规则
	 */
	private String listNameRule;

	/**
	 * 详情页命名规则
	 */
	private String detailNameRule;

	/**
	 * 栏目层级
	 */
	private Integer treeLevel;

	/**
	 * 子栏目数
	 */
	private Integer childCount;

	/**
	 * 内容数量
	 */
	private Integer contentCount;

	/**
	 * 状态
	 */
	private String status;

	/**
	 * 点击量
	 */
	private Integer hitCount;

	/**
	 * SEO关键词
	 */
	private String seoKeywords;

	/**
	 * SEO描述
	 */
	private String seoDescription;

	/**
	 * SEO标题
	 */
	private String seoTitle;

	/**
	 * 链接（非表字段）
	 */
	@TableField(exist = false)
	private String link;
	
	/**
	 * 列表页链接（无首页模板时与link一致）
	 */
	@TableField(exist = false)
	private String listLink;
	
	/**
	 * 发布通道配置
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<String, Map<String, Object>> publishPipeProps;

	/**
	 * 扩展配置
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<String, String> configProps;

	public Map<String, String> getConfigProps() {
		if (this.configProps == null) {
			this.configProps = new HashMap<>();
		}
		return configProps;
	}

	public Map<String, Object> getPublishPipeProps(String publishPipeCode) {
		if (this.publishPipeProps == null) {
			this.publishPipeProps = new HashMap<>();
		}
		Map<String, Object> map = this.publishPipeProps.get(publishPipeCode);
		if (map == null) {
			map = new HashMap<>();
			this.publishPipeProps.put(publishPipeCode, map);
		}
		return map;
	}

	public String getIndexTemplate(String publishPipeCode) {
		return PublishPipeProp_IndexTemplate.getValue(publishPipeCode, this.publishPipeProps);
	}

	public String getListTemplate(String publishPipeCode) {
		return PublishPipeProp_ListTemplate.getValue(publishPipeCode, this.publishPipeProps);
	}
	
	public boolean isStaticize() {
		return YesOrNo.isYes(this.staticFlag);
	}
	
	public boolean isVisible() {
		return YesOrNo.isYes(this.visibleFlag);
	}
	
	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
}
