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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentOpType;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter 
@Setter
public class ContentDTO {
	
	/**
	 * 操作类型
	 */
	private ContentOpType opType;

	/**
	 * 内容ID
	 */
	private Long contentId;

	/**
	 * 栏目ID
	 */
	private Long catalogId;

    /**
     * 内容类型
     */
	private String contentType;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 副标题
	 */
    private String subTitle;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * 标题样式
     */
    private String titleStyle;

    /**
     * 引导图
     */
    private String logo;

    /**
     * 引导图预览路径
     */
    private String logoSrc;
    
    /**
     * 发布链接
     */
    private String link;

    /**
     * 来源名称
     */
    private String source;

    /**
     * 来源URL
     */
    private String sourceUrl;

    /**
     * 是否原创
     */
    private String original;

    /**
     * 作者
     */
    private String author;

    /**
     * 编辑
     */
    private String editor;
	
    /**
	 * 摘要
	 */
    private String summary;

    /**
     * 是否链接内容
     */
    private String linkFlag;

    /**
     * 链接
     */
    private String redirectUrl;

	/**
	 * 指定标识
	 */
	private Long topFlag;

    /**
     * 内容属性值数组
     */
    private String[] attributes;

    /**
     * 关键词
     */
    private String[] keywords;

    /**
     * TAG
     */
    private String[] tags;

    /**
     * 发布时间
     */
    private LocalDateTime publishDate;

    /**
     * 下线时间
     */
    private LocalDateTime offlineDate;

	/**
	 * 点赞数
	 */
	private Long likeCount;

	/**
	 * 评论数
	 */
	private Long commentCount;

	/**
	 * 收藏数
	 */
	private Long favoriteCount;

	/**
	 * 文章浏览数
	 */
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
	private List<PublishPipeProp> publishPipeProps;
	
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
	private String status;

	/**
	 * SEO标题
	 */
	private String seoTitle;

	/**
	 * SEO关键词
	 */
	private String seoKeywords;

	/**
	 * SEO描述
	 */
	private String seoDescription;

	/**
	 * 备用字段1
	 */
	private String prop1;

	/**
	 * 备用字段2
	 */
	private String prop2;

	/**
	 * 备用字段3
	 */
	private String prop3;

	/**
	 * 备用字段4
	 */
	private String prop4;
	
	public static ContentDTO newInstance(CmsContent cmsContent) {
		ContentDTO dto = new ContentDTO();
		BeanUtils.copyProperties(cmsContent, dto);
		dto.setAttributes(ContentAttribute.convertStr(cmsContent.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
		return dto;
	}
}
