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
package com.chestnut.cms.stat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 栏目内容统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsCatalogContentStat.TABLE_NAME)
public class CmsCatalogContentStat implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_catalog_content_stat";

	@TableId(value = "catalog_id", type = IdType.INPUT)
	private Long catalogId;

	/**
	 * 站点ID
	 */
	private Long siteId;

	@TableField(exist = false)
	private Long parentId;

	@TableField(exist = false)
	private String name;

	/**
	 * 初稿内容数量
	 */
	private Integer draftTotal;

	/**
	 * 待发布内容数量
	 */
	private Integer toPublishTotal;

	/**
	 * 已发布内容数量
	 */
	private Integer publishedTotal;

	/**
	 * 已下线内容数量
	 */
	private Integer offlineTotal;

	/**
	 * 重新编辑内容数量
	 */
	private Integer editingTotal;
}
