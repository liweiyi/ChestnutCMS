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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneOffset;

@Getter 
@Setter
public class ContentApiVO {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 栏目ID
     */
    private Long catalogId;

    /**
     * 栏目名称
     */
    private String catalogName;

    /**
     * 栏目链接
     */
    private String catalogLink;

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
     * 内容属性值数组
     */
    private String[] attributes;

    /**
     * 置顶标识
     */
    private Long topFlag;

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
    private Long publishDate;

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

    protected void copyProperties(CmsContent content) {
        this.setAuthor(content.getAuthor());
        this.setCatalogId(content.getCatalogId());
        this.setContentId(content.getContentId());
        this.setContentType(content.getContentType());
        this.setEditor(content.getEditor());
        this.setKeywords(content.getKeywords());
        this.setLogo(content.getLogo());
        this.setOriginal(content.getOriginal());
        this.setPublishDate(content.getPublishDate().toInstant(ZoneOffset.UTC).toEpochMilli());
        this.setShortTitle(content.getShortTitle());
        this.setSubTitle(content.getSubTitle());
        this.setTitle(content.getTitle());
        this.setSource(content.getSource());
        this.setSourceUrl(content.getSourceUrl());
        this.setSummary(content.getSummary());
        this.setTags(content.getTags());
        this.setTitleStyle(content.getTitleStyle());
        this.setTopFlag(content.getTopFlag());
        this.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
        this.setViewCount(content.getViewCount());
        this.setLikeCount(content.getLikeCount());
        this.setCommentCount(content.getCommentCount());
        this.setFavoriteCount(content.getFavoriteCount());
    }

    public static ContentApiVO newInstance(CmsContent content) {
        ContentApiVO vo = new ContentApiVO();
        vo.copyProperties(content);
        return vo;
    }
}
