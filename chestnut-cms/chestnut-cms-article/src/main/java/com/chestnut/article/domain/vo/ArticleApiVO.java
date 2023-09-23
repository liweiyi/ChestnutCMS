package com.chestnut.article.domain.vo;

import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentApiVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneOffset;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ArticleApiVO extends ContentApiVO {

    private String contentHtml;

    public static ArticleApiVO newInstance(CmsContent cmsContent) {
        ArticleApiVO dto = new ArticleApiVO();
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
