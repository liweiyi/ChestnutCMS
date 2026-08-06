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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.InitByContent;
import com.chestnut.contentcore.fixed.dict.ContentCopyType;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@XComment("{CMS.CONTENT.LIST_ITEM}")
public class ListContentVO implements InitByContent {

	/*
	 * 内容ID
	 */
	@XComment("{CMS.CONTENT.ID}")
	private Long contentId;

	/*
	 * 内容ID
	 */
	@XComment("{CMS.CONTENT.CATALOG_ID}")
	private Long catalogId;

	/*
	 * 内容类型
	 */
	@XComment("{CMS.CONTENT.TYPE}")
	private String contentType;

	/*
	 * 标题
	 */
	@XComment("{CMS.CONTENT.TITLE}")
	private String title;

	/*
	 * 短标题
	 */
	@XComment("{CMS.CONTENT.SHORT_TITLE}")
	private String shortTitle;

	/*
	 * 副标题
	 */
	@XComment("{CMS.CONTENT.SUB_TITLE}")
	private String subTitle;

	/*
	 * 标题样式
	 */
    @XComment("{CMS.CONTENT.TITLE_STYLE}")
    private String titleStyle;

	/*
	 * 引导图
	 */
    @XComment("{CMS.CONTENT.LOGO}")
    private String logo;

	/*
	 * 引导图预览路径
	 */
    @XComment("{CMS.CONTENT.LOGO_SRC}")
    private String logoSrc;

	/*
	 * 引导图
	 */
	@XComment("{CMS.CONTENT.IMAGES}")
	private List<String> images;

	/*
	 * 引导图预览路径
	 */
	@XComment("{CMS.CONTENT.IMAGES_SRC}")
	private List<String> imagesSrc;

	/*
	 * 内部链接
	 */
    @XComment("{CMS.CONTENT.INTERNAL_URL}")
    private String internalUrl;

	/*
	 * 是否原创
	 */
    @XComment("{CMS.CONTENT.ORIGINAL}")
    private String original;

	/*
	 * 作者
	 */
    @XComment("{CMS.CONTENT.AUTHOR}")
    private String author;

	/*
	 * 编辑
	 */
    @XComment("{CMS.CONTENT.EDITOR}")
    private String editor;

	/*
	 * 内容状态
	 */
    @XComment("{CMS.CONTENT.STATUS}")
    private String status;

	/*
	 * 内容属性值数组
	 */
    @XComment("{CMS.CONTENT.ATTRS}")
    private String[] attributes;

	/*
	 * 关键词
	 */
	@XComment("{CMS.CONTENT.KEYWORDS}")
	private String[] keywords;

	/*
	 * TAGs
	 */
	@XComment("{CMS.CONTENT.TAGS}")
	private String[] tags;

	/*
	 * 摘要
	 */
	@XComment("{CMS.CONTENT.SUMMARY}")
	private String summary;

	/*
	 * 置顶标识
	 */
    @XComment("{CMS.CONTENT.TOP_FLAG}")
    private Long topFlag;

	/*
	 * 置顶时间
	 */
    @XComment("{CMS.CONTENT.TOP_DATE}")
    private Date topDate;

	/*
	 * 是否锁定
	 */
    @XComment("{CMS.CONTENT.IS_LOCK}")
    private String isLock;

	/*
	 * 是否锁定
	 */
    @XComment("{CMS.CONTENT.LOCK_USER}")
    private String lockUser;

	/*
	 * 复制类型
	 */
    @XComment("{CMS.CONTENT.COPY_TYPE}")
    private Integer copyType;

	/*
	 * 复制源ID
	 */
    @XComment("{CMS.CONTENT.COPY_ID}")
    private Long copyId;

    /*
	 * 复制信息
	 */
    @XComment("{CMS.CONTENT.COPY_INFO}")
    private ContentCopyType.ContentCopyInfo copyInfo;

	/*
	 * 发布时间
	 */
    @XComment("{CMS.CONTENT.PUBLISH_DATE}")
    private LocalDateTime publishDate;

	/*
	 * 下线时间
	 */
    @XComment("{CMS.CONTENT.OFFLINE_DATE}")
    private LocalDateTime offlineDate;

	/**
	 * 点赞数
	 */
	@XComment("{CMS.CONTENT.LIKE}")
	private Long likeCount;

	/**
	 * 评论数
	 */
	@XComment("{CMS.CONTENT.COMMENT}")
	private Long commentCount;

	/**
	 * 收藏数
	 */
	@XComment("{CMS.CONTENT.FAVORITE}")
	private Long favoriteCount;

	/**
	 * 文章浏览数
	 */
	@XComment("{CMS.CONTENT.VIEW}")
	private Long viewCount;

	/*
	 * 创建人
	 */
    @XComment("{CC.ENTITY.CREATE_BY}")
    private String createUser;

	/*
	 * 创建时间
	 */
	@XComment("{CC.ENTITY.CREATE_TIME}")
	private LocalDateTime createTime;

	public static ListContentVO newInstance(CmsContent content) {
		ListContentVO vo = new ListContentVO();
		vo.initByContent(content, true);
		vo.setInternalUrl(InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, content.getContentId()));
		return vo;
	}
}
