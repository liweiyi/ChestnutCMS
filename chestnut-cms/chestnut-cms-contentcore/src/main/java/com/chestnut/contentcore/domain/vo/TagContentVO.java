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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容标签数据对象
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter 
@Setter
public class TagContentVO extends TagBaseVO {

    @XComment("{CMS.CONTENT.ID}")
    private Long contentId;

    @XComment("{CMS.CONTENT.SITE_ID}")
    private Long siteId;

    @XComment("{CMS.CONTENT.CATALOG_ID}")
    private Long catalogId;

    @XComment("{CMS.CONTENT.CATALOG_ANCESTORS}")
    private String catalogAncestors;

    @XComment("{CMS.CONTENT.TOP_CATALOG}")
    private Long topCatalog;

    @XComment("{CMS.CONTENT.DEPT_ID}")
    private Long deptId;

    @XComment("{CMS.CONTENT.DEPT_CODE}")
    private String deptCode;

    @XComment("{CMS.CONTENT.TYPE}")
    private String contentType;

    @XComment("{CMS.CONTENT.TITLE}")
    private String title;

    @XComment("{CMS.CONTENT.SUB_TITLE}")
    private String subTitle;

    @XComment("{CMS.CONTENT.SHORT_TITLE}")
    private String shortTitle;

    @XComment("{CMS.CONTENT.SHORT_TITLE}")
    private String titleStyle;

    @XComment(value = "{CMS.CONTENT.LOGO}")
    private String logo;

    @XComment(value = "{CMS.CONTENT.LOGO_SRC}")
    private String logoSrc;

    @XComment("{CMS.CONTENT.IMAGES}")
    private List<String> images;

    @XComment("{CMS.CONTENT.SOURCE}")
    private String source;

    @XComment("{CMS.CONTENT.SOURCE_URL}")
    private String sourceUrl;

    @XComment("{CMS.CONTENT.ORIGINAL}")
    private String original;

    @XComment("{CMS.CONTENT.AUTHOR}")
    private String author;

    @XComment("{CMS.CONTENT.EDITOR}")
    private String editor;

    @XComment("{CMS.CONTENT.CONTRIBUTOR_ID}")
    private Long contributorId;

    @XComment("{CMS.CONTENT.SUMMARY}")
    private String summary;

    @XComment("{CMS.CONTENT.ATTRS}")
    private String[] attributes;

    @XComment("{CMS.CONTENT.STATUS}")
    private String status;

    @XComment("{CMS.CONTENT.LINK_FLAG}")
    private String linkFlag;

    @XComment("{CMS.CONTENT.REDIRECT_URL}")
    private String redirectUrl;

    @XComment("{CMS.CONTENT.TOP_FLAG}")
    private Long topFlag;

    @XComment("{CMS.CONTENT.TOP_DATE}")
    private LocalDateTime topDate;

    @XComment("{CC.ENTITY.SORT}")
    private Long sortFlag;

    @XComment("{CMS.CONTENT.KEYWORDS}")
    private String[] keywords;

    @XComment("{CMS.CONTENT.TAGS}")
    private String[] tags;

    @XComment("{CMS.CONTENT.PUBLISH_DATE}")
    private LocalDateTime publishDate;

    @XComment("{CMS.CONTENT.SEO_TITLE}")
    private String seoTitle;

    @XComment("{CMS.CONTENT.SEO_KEYWORDS}")
    private String seoKeywords;

    @XComment("{CMS.CONTENT.SEO_DESC}")
    private String seoDescription;

    @XComment("{CMS.CONTENT.LIKE}")
    private Long likeCount;

    @XComment("{CMS.CONTENT.COMMENT}")
    private Long commentCount;

    @XComment("{CMS.CONTENT.FAVORITE}")
    private Long favoriteCount;

    @XComment("{CMS.CONTENT.VIEW}")
    private Long viewCount;

    @XComment("{CMS.CONTENT.PROP1}")
    private String prop1;

    @XComment("{CMS.CONTENT.PROP2}")
    private String prop2;

    @XComment("{CMS.CONTENT.PROP3}")
    private String prop3;

    @XComment("{CMS.CONTENT.PROP4}")
    private String prop4;

    @XComment("{CMS.CONTENT.LINK}")
    private String link;

    public static TagContentVO newInstance(CmsContent content, String publishPipeCode, boolean preview) {
        TagContentVO vo = new TagContentVO();
        BeanUtils.copyProperties(content, vo);
        vo.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
        if (StringUtils.isNotEmpty(content.getImages())) {
            // 兼容历史版本
            vo.setLogo(content.getImages().get(0));
            vo.setLogoSrc(InternalUrlUtils.getActualUrl(content.getLogo(), publishPipeCode, preview));
        }
        return vo;
    }
}
