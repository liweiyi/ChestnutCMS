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
package com.chestnut.cms.image.domain.vo;

import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.domain.vo.TagBaseVO;
import lombok.Getter;
import lombok.Setter;

/**
 * TagImageVO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagImageVO extends TagBaseVO {

    @XComment("图片ID")
    private Long imageId;

    @XComment("所属内容ID")
    private Long contentId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("视频标题")
    private String title;

    @XComment("简介")
    private String description;

    @XComment("图片原文件名")
    private String fileName;

    @XComment("图片路径")
    private String path;

    @XComment(value = "访问路径", deprecated = true, forRemoval = "1.6.0")
    private String src;

    @XComment("图片类型")
    private String imageType;

    @XComment("图片大小")
    private Long fileSize;

    @XComment("图片宽度")
    private Integer width;

    @XComment("图片高度")
    private Integer height;

    @XComment("跳转链接")
    private String redirectUrl;

    @XComment("排序值")
    private Integer sortFlag;

    public static TagImageVO newInstance(CmsImage image) {
        TagImageVO vo = new TagImageVO();
        vo.setImageId(image.getImageId());
        vo.setContentId(image.getContentId());
        vo.setSiteId(image.getSiteId());
        vo.setTitle(image.getTitle());
        vo.setDescription(image.getDescription());
        vo.setFileName(image.getFileName());
        vo.setPath(image.getPath());
        vo.setImageType(image.getImageType());
        vo.setFileSize(image.getFileSize());
        vo.setWidth(image.getWidth());
        vo.setHeight(image.getHeight());
        vo.setRedirectUrl(image.getRedirectUrl());
        vo.setSortFlag(image.getSortFlag());
        vo.setCreateBy(image.getCreateBy());
        vo.setUpdateBy(image.getUpdateBy());
        vo.setCreateTime(image.getCreateTime());
        vo.setUpdateTime(image.getUpdateTime());
        vo.setRemark(image.getRemark());
        return vo;
    }
}
