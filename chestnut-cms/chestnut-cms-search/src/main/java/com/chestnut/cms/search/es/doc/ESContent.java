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
package com.chestnut.cms.search.es.doc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ESContent {
	
	private Long contentId;

	private String contentType;

	private Long siteId;

	private Long catalogId;

	private String catalogAncestors;

	private String logo;

	private String title;

	private String link;

	private String status;

	private String author;

	private String editor;

	private String keywords;

	private String tags;
	
	/**
	 * 内容详情，需要分词处理的数据
	 */
	private String fullText;

	private Long publishDate;

	private Long createTime;
}
