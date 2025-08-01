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
package com.chestnut.cms.search.vo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ESContentVO {

	@XComment("{CMS.CONTENT.ID}")
	private Long contentId;

	@XComment("{CMS.CONTENT.TYPE}")
	private String contentType;

	@XComment("{CMS.CONTENT.SITE_ID}")
	private Long siteId;

	@XComment("{CMS.CONTENT.CATALOG_ID}")
	private Long catalogId;

	@XComment("{CMS.CATALOG.NAME}")
	private String catalogName;

	@XComment("{CMS.CONTENT.CATALOG_ANCESTORS}")
	private String catalogAncestors;

	@XComment("{CMS.CONTENT.LOGO}")
	private String logo;

	@XComment(value = "{CMS.CONTENT.TITLE}")
	private String title;

	@XComment(value = "{CMS.CONTENT.SUMMARY}")
	private String summary;

	@XComment(value = "{CMS.CONTENT.LINK}")
	private String link;

	@XComment(value = "{CMS.CONTENT.STATUS}")
	private String status;

	@XComment(value = "{CMS.CONTENT.AUTHOR}")
	private String author;

	@XComment(value = "{CMS.CONTENT.EDITOR}")
	private String editor;

	@XComment(value = "{CMS.CONTENT.KEYWORDS}")
	private String[] keywords;

	@XComment(value = "{CMS.CONTENT.TAGS}")
	private String[] tags;

	@XComment(value = "{CMS.ES_CONTENT.FULL_TEXT}")
	private String fullText;

	@XComment(value = "{CMS.ES_CONTENT.PUBLISH_DATE_TIMESTAMP}")
	private Long publishDate;

	@XComment(value = "{CMS.CONTENT.PUBLISH_DATE}")
	private LocalDateTime publishDateInstance;

	@XComment(value = "{CMS.ES_CONTENT.CREATE_TIME_TIMESTAMP}")
	private Long createTime;

	@XComment(value = "{CC.ENTITY.CREATE_TIME}")
	private LocalDateTime createTimeInstance;

	@XComment(value = "{CMS.ES_CONTENT.HIT_SCORE}")
	private Double hitScore;

	@XComment("{CMS.CONTENT.LIKE}")
	private Long likeCount;

	@XComment("{CMS.CONTENT.COMMENT}")
	private Long commentCount;

	@XComment("{CMS.CONTENT.FAVORITE}")
	private Long favoriteCount;

	@XComment("{CMS.CONTENT.VIEW}")
	private Long viewCount;

	@XComment("{CMS.ES_CONTENT.EXTEND_DATA}")
	private Map<String, Object> extendData = new HashMap<>();
}
