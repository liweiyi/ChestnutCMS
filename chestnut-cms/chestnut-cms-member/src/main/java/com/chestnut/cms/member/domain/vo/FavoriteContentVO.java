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
package com.chestnut.cms.member.domain.vo;

import com.chestnut.contentcore.domain.CmsContent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会员收藏内容数据VO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class FavoriteContentVO {

    private Long contentId;

    private Long siteId;

    private Long catalogId;

    private String contentType;

    private String title;

    private String shortTitle;

    private String subTitle;

    private String logo;

    private String summary;

    private LocalDateTime publishDate;

    private String source;

    private String sourceUrl;

    private Long topFlag;

    private String[] tags;

    private String[] keywords;

    private Long likeCount;

    private Long commentCount;

    private Long favoriteCount;

    private Long viewCount;

    public static FavoriteContentVO newInstance(CmsContent content) {
        FavoriteContentVO vo = new FavoriteContentVO();
        vo.setContentId(content.getContentId());
        vo.setSiteId(content.getSiteId());
        vo.setCatalogId(content.getCatalogId());
        vo.setContentType(content.getContentType());
        vo.setTitle(content.getTitle());
        vo.setShortTitle(content.getShortTitle());
        vo.setSubTitle(content.getSubTitle());
        vo.setLogo(content.getLogo());
        vo.setSummary(content.getSummary());
        vo.setPublishDate(content.getPublishDate());
        vo.setTopFlag(content.getTopFlag());
        vo.setSource(content.getSource());
        vo.setSourceUrl(content.getSourceUrl());
        vo.setTags(content.getTags());
        vo.setKeywords(content.getKeywords());
        vo.setLikeCount(content.getLikeCount());
        vo.setCommentCount(content.getCommentCount());
        vo.setFavoriteCount(content.getFavoriteCount());
        vo.setViewCount(content.getViewCount());
        return vo;
    }
}
