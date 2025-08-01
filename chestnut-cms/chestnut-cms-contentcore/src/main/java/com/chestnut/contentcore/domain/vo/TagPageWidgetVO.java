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
import com.chestnut.contentcore.domain.CmsPageWidget;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * 栏目标签数据对象
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagPageWidgetVO {

	@XComment("{CMS.PAGE_WIDGET.ID}")
	private Long pageWidgetId;

	@XComment("{CMS.PAGE_WIDGET.SITE_ID}")
	private Long siteId;

	@XComment("{CMS.PAGE_WIDGET.CATALOG_ID}")
	private Long catalogId;

	@XComment("{CMS.PAGE_WIDGET.CATALOG_ANCESTORS}")
	private String catalogAncestors;

	@XComment("{CMS.PAGE_WIDGET.TYPE}")
	private String type;

	@XComment("{CMS.PAGE_WIDGET.NAME}")
	private String name;

	@XComment("{CMS.PAGE_WIDGET.CODE}")
	private String code;

	@XComment(value = "{CMS.PAGE_WIDGET.PUBLISH_PIPE}", deprecated = true, forRemoval = "1.6.0")
	private String publishPipeCode;

	@XComment("{CMS.PAGE_WIDGET.CONTENT_OBJ}")
	private Object contentObj;

	public static TagPageWidgetVO newInstance(CmsPageWidget pageWidget) {
		TagPageWidgetVO vo = new TagPageWidgetVO();
		BeanUtils.copyProperties(pageWidget, vo);
		return vo;
	}
}
