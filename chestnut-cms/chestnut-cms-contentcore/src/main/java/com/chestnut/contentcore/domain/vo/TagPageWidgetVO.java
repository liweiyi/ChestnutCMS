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

	@XComment("页面部件ID")
	private Long pageWidgetId;

	@XComment("所属站点ID")
	private Long siteId;

	@XComment("所属栏目ID")
	private Long catalogId;

	@XComment("所属栏目祖级IDs")
	private String catalogAncestors;

	@XComment("类型")
	private String type;

	@XComment("名称")
	private String name;

	@XComment("编码")
	private String code;

	@XComment("发布通道")
	private String publishPipeCode;

	@XComment("页面部件扩展数据")
	private Object contentObj;

	public static TagPageWidgetVO newInstance(CmsPageWidget pageWidget) {
		TagPageWidgetVO vo = new TagPageWidgetVO();
		BeanUtils.copyProperties(pageWidget, vo);
		return vo;
	}
}
