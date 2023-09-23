package com.chestnut.cms.search.es.doc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ESContent {
	
	public static final String INDEX_NAME = "cms_content";

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
