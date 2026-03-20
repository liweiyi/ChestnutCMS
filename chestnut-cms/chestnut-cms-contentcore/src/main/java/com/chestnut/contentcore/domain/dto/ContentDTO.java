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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.InitByContent;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.fixed.dict.ContentOpType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XComment("{API.DOC.CMS.CONTENT.CONTENT_DTO}")
@Getter 
@Setter
public class ContentDTO implements InitByContent {
	
	/**
	 * 操作类型
	 */
	@XComment("{API.DOC.CMS.CONTENT.OP_TYPE}")
	private String opType;

	/**
	 * 内容ID
	 */
	@XComment("{CMS.CONTENT.ID}")
	private Long contentId;

	/**
	 * 栏目ID
	 */
	@XComment("{CMS.CONTENT.CATALOG_ID}")
	private Long catalogId;

    /**
     * 内容类型
     */
	@XComment("{CMS.CONTENT.TYPE}")
	private String contentType;

	/**
	 * 标题
	 */
	@XComment("{CMS.CONTENT.TITLE}")
	private String title;

	/**
	 * 副标题
	 */
	@XComment("{CMS.CONTENT.SUB_TITLE}")
    private String subTitle;

    /**
     * 短标题
     */
	@XComment("{CMS.CONTENT.SHORT_TITLE}")
    private String shortTitle;

    /**
     * 标题样式
     */
	@XComment("{CMS.CONTENT.TITLE_STYLE}")
    private String titleStyle;

    /**
     * 封面图
     */
	@XComment("{CMS.CONTENT.LOGO}")
    private String logo;

    /**
     * 封面图预览路径
     */
	@XComment("{CMS.CONTENT.LOGO_SRC}")
    private String logoSrc;

	/**
	 * 其他图片
	 */
	@XComment("{CMS.CONTENT.IMAGES}")
	private List<String> images = List.of();

	/**
	 * 其他图片预览路径
	 */
	private List<String> imagesSrc = List.of();
    
    /**
     * 发布链接
     */
	@XComment("{CMS.CONTENT.LINK}")
    private String link;

    /**
     * 来源名称
     */
	@XComment("{CMS.CONTENT.SOURCE}")
    private String source;

    /**
     * 来源URL
     */
	@XComment("{CMS.CONTENT.SOURCE_URL}")
    private String sourceUrl;

    /**
     * 是否原创
     */
	@XComment("{CMS.CONTENT.ORIGINAL}")
    private String original;

    /**
     * 作者
     */
	@XComment("{CMS.CONTENT.AUTHOR}")
    private String author;

    /**
     * 编辑
     */
	@XComment("{CMS.CONTENT.EDITOR}")
    private String editor;
	
    /**
	 * 摘要
	 */
	@XComment("{CMS.CONTENT.SUMMARY}")
    private String summary;

    /**
     * 是否链接内容
     */
	@XComment("{CMS.CONTENT.LINK_FLAG}")
    private String linkFlag;

    /**
     * 链接
     */
	@XComment("{CMS.CONTENT.REDIRECT_URL}")
    private String redirectUrl;

	/**
	 * 指定标识
	 */
	@XComment("{CMS.CONTENT.TOP_FLAG}")
	private Long topFlag;

    /**
     * 内容属性值数组
     */
	@XComment("{CMS.CONTENT.ATTRS}")
    private String[] attributes;

    /**
     * 关键词
     */
	@XComment("{CMS.CONTENT.KEYWORDS}")
    private String[] keywords;

    /**
     * TAG
     */
	@XComment("{CMS.CONTENT.TAGS}")
    private String[] tags;

    /**
     * 发布时间
     */
	@XComment("{CMS.CONTENT.PUBLISH_DATE}")
    private LocalDateTime publishDate;

    /**
     * 下线时间
     */
	@XComment("{API.DOC.CMS.CONTENT.OFFLINE_DATE}")
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

    /**
     * 发布通道
     */
    private String[] publishPipe;
    
    /**
     * 独立模板
     */
    private Map<String, Object> template;

	/**
	 * 独立路径
	 */
	private String staticPath;

	/**
     * 备注
     */
	private String remark;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	
	/**
	 * 发布通道配置
	 */
	private List<PublishPipeProps> publishPipeProps;
	
	/**
	 * 栏目扩展配置
	 */
	private Map<String, Object> catalogConfigProps = new HashMap<>(); 
	
	/**
	 * 自定义参数
	 */
	private Map<String, Object> params;

	/**
	 * 状态
	 */
	@XComment("{CMS.CONTENT.STATUS}")
	private String status;

	/**
	 * SEO标题
	 */
	@XComment("{CMS.CONTENT.SEO_TITLE}")
	private String seoTitle;

	/**
	 * SEO关键词
	 */
	@XComment("{CMS.CONTENT.SEO_KEYWORDS}")
	private String seoKeywords;

	/**
	 * SEO描述
	 */
	@XComment("{CMS.CONTENT.SEO_DESC}")
	private String seoDescription;

	/**
	 * 备用字段1
	 */
	@XComment("{CMS.CONTENT.PROP1}")
	private String prop1;

	/**
	 * 备用字段2
	 */
	@XComment("{CMS.CONTENT.PROP2}")
	private String prop2;

	/**
	 * 备用字段3
	 */
	@XComment("{CMS.CONTENT.PROP3}")
	private String prop3;

	/**
	 * 备用字段4
	 */
	@XComment("{CMS.CONTENT.PROP4}")
	private String prop4;

	public CmsContent convertToContentEntity(ICatalogService catalogService, IContentService contentService) {
		CmsContent contentEntity;
		if (ContentOpType.UPDATE.equals(this.getOpType())) {
			contentEntity = contentService.dao().getById(this.getContentId());
			Assert.notNull(contentEntity,
					() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", this.getContentId()));
		} else {
			contentEntity = new CmsContent();
		}
		BeanUtils.copyProperties(this, contentEntity);
		// 所属站点
		CmsCatalog catalog = catalogService.getCatalog(this.getCatalogId());
		contentEntity.setSiteId(catalog.getSiteId());
		contentEntity.setAttributes(ContentAttribute.convertInt(this.getAttributes()));
		// 发布通道配置
		Map<String, Map<String, Object>> publishPipProps = new HashMap<>();
		this.getPublishPipeProps().forEach(prop -> {
			publishPipProps.put(prop.getPipeCode(), prop.getProps());
		});
		contentEntity.setPublishPipeProps(publishPipProps);
		return contentEntity;
	}
	
	public static ContentDTO newInstance(CmsContent cmsContent) {
		ContentDTO dto = new ContentDTO();
		dto.initByContent(cmsContent, false);
		return dto;
	}
}
