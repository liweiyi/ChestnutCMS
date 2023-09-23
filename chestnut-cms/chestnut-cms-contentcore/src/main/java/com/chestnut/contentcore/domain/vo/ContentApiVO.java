package com.chestnut.contentcore.domain.vo;

import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static ContentApiVO newInstance(CmsContent cmsContent) {
        ContentApiVO dto = new ContentApiVO();
        dto.setAuthor(cmsContent.getAuthor());
        dto.setCatalogId(cmsContent.getCatalogId());
        dto.setContentId(cmsContent.getContentId());
        dto.setContentType(cmsContent.getContentType());
        dto.setEditor(cmsContent.getEditor());
        dto.setKeywords(cmsContent.getKeywords());
        dto.setLogo(cmsContent.getLogo());
        dto.setOriginal(cmsContent.getOriginal());
        dto.setPublishDate(cmsContent.getPublishDate().toInstant(ZoneOffset.UTC).toEpochMilli());
        dto.setShortTitle(cmsContent.getShortTitle());
        dto.setSubTitle(cmsContent.getSubTitle());
        dto.setTitle(cmsContent.getTitle());
        dto.setSource(cmsContent.getSource());
        dto.setSourceUrl(cmsContent.getSourceUrl());
        dto.setSummary(cmsContent.getSummary());
        dto.setTags(cmsContent.getTags());
        dto.setTitleStyle(cmsContent.getTitleStyle());
        dto.setTopFlag(cmsContent.getTopFlag());
        dto.setAttributes(ContentAttribute.convertStr(cmsContent.getAttributes()));
        dto.setViewCount(cmsContent.getViewCount());
        dto.setLikeCount(cmsContent.getLikeCount());
        dto.setCommentCount(cmsContent.getCommentCount());
        dto.setFavoriteCount(cmsContent.getFavoriteCount());
        return dto;
    }
}
