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

    @XComment("内容ID")
    private Long contentId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("所属栏目ID")
    private Long catalogId;

    @XComment("所属栏目祖级IDs")
    private String catalogAncestors;

    @XComment("所属顶级栏目")
    private Long topCatalog;

    @XComment("所属部门ID")
    private Long deptId;

    @XComment("所属部门编码")
    private String deptCode;

    @XComment("内容类型")
    private String contentType;

    @XComment("标题")
    private String title;

    @XComment("副标题")
    private String subTitle;

    @XComment("短标题")
    private String shortTitle;

    @XComment("标题样式")
    private String titleStyle;

    @XComment(value = "封面图", deprecated = true, forRemoval = "1.6.0")
    private String logo;

    @XComment(value = "封面图访问路径", deprecated = true, forRemoval = "1.6.0")
    private String logoSrc;

    @XComment("封面图列表")
    private List<String> images;

    @XComment("来源")
    private String source;

    @XComment("来源URL")
    private String sourceUrl;

    @XComment("是否原创")
    private String original;

    @XComment("作者")
    private String author;

    @XComment("编辑")
    private String editor;

    @XComment("投稿用户ID")
    private Long contributorId;

    @XComment("摘要")
    private String summary;

    @XComment("内容属性标识列表")
    private String[] attributes;

    @XComment("是否链接内容")
    private String linkFlag;

    @XComment("跳转链接（linkFlag==Y）")
    private String redirectUrl;

    @XComment("置顶标识")
    private Long topFlag;

    @XComment("置顶结束时间")
    private LocalDateTime topDate;

    @XComment("排序值")
    private Long sortFlag;

    @XComment("关键词")
    private String[] keywords;

    @XComment("TAGs")
    private String[] tags;

    @XComment("发布时间")
    private LocalDateTime publishDate;

    @XComment("SEO标题")
    private String seoTitle;

    @XComment("SEO关键词")
    private String seoKeywords;

    @XComment("SEO描述")
    private String seoDescription;

    @XComment("点赞数（非实时）")
    private Long likeCount;

    @XComment("评论数（非实时）")
    private Long commentCount;

    @XComment("收藏数（非实时）")
    private Long favoriteCount;

    @XComment("文章浏览数（非实时）")
    private Long viewCount;

    @XComment("备用字段1")
    private String prop1;

    @XComment("备用字段2")
    private String prop2;

    @XComment("备用字段3")
    private String prop3;

    @XComment("备用字段4")
    private String prop4;

    @XComment("内容链接")
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
