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
package com.chestnut.cms.stat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台用户内容统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsUserContentStat.TABLE_NAME)
public class CmsUserContentStat implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_user_content_stat";

	@TableId(value = "id", type = IdType.INPUT)
	private String id;

	/**
	 * 站点ID
	 */
	private Long siteId;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 用户名
	 */
	private String userName;

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

	public void changeStatusTotal(String status, int total) {
		switch(status) {
			case ContentStatus.DRAFT -> draftTotal = total;
			case ContentStatus.TO_PUBLISHED -> toPublishTotal = total;
			case ContentStatus.PUBLISHED -> publishedTotal = total;
			case ContentStatus.OFFLINE -> offlineTotal = total;
			case ContentStatus.EDITING -> editingTotal = total;
		}
	}
}
