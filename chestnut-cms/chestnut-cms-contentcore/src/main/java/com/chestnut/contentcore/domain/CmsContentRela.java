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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 关联内容表对象 [cms_content_rela]
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(CmsContentRela.TABLE_NAME)
public class CmsContentRela extends BaseEntity {

	public static final String TABLE_NAME = "cms_content_rela";

	@TableId(value = "rela_id", type = IdType.INPUT)
	private Long relaId;

	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 内容ID
	 */
	private Long contentId;

	/**
	 * 关联内容ID
	 */
	private Long relaContentId;
}
