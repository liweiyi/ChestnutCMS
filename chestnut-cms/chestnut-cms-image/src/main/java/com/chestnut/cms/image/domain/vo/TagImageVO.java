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

    @XComment("{CMS.IMAGE.ID}")
    private Long imageId;

    @XComment("{CMS.IMAGE.CONTENT_ID}")
    private Long contentId;

    @XComment("{CMS.IMAGE.SITE_ID}")
    private Long siteId;

    @XComment("{CMS.IMAGE.TITLE}")
    private String title;

    @XComment("{CMS.IMAGE.DESC}")
    private String description;

    @XComment("{CMS.IMAGE.FILE_NAME}")
    private String fileName;

    @XComment("{CMS.IMAGE.PATH}")
    private String path;

    @XComment(value = "{CMS.IMAGE.SRC}", deprecated = true, forRemoval = "1.6.0")
    private String src;

    @XComment("{CMS.IMAGE.TYPE}")
    private String imageType;

    @XComment("{CMS.IMAGE.FILE_SIZE}")
    private Long fileSize;

    @XComment("{CMS.IMAGE.WIDTH}")
    private Integer width;

    @XComment("{CMS.IMAGE.HEIGHT}")
    private Integer height;

    @XComment("{CMS.IMAGE.REDIRECT_URL}")
    private String redirectUrl;

    @XComment("{CC.ENTITY.SORT}")
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
