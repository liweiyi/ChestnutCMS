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

import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.InitByContent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter 
@Setter
public class ContentApiVO implements InitByContent {

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
     * 封面图
     */
    private String logo;

    /**
     * 封面图预览路径
     */
    private String logoSrc;

    /**
     * 其他图片
     */
    private List<String> images = List.of();

    /**
     * 其他图片预览路径
     */
    private List<String> imagesSrc = List.of();
    
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

    public static ContentApiVO newInstance(CmsContent content) {
        ContentApiVO vo = new ContentApiVO();
        vo.initByContent(content, false);
        return vo;
    }
}
