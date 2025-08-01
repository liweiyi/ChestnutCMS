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
package com.chestnut.media.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.domain.vo.TagBaseVO;
import com.chestnut.media.domain.CmsVideo;
import lombok.Getter;
import lombok.Setter;

/**
 * TagVideoVO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagVideoVO extends TagBaseVO {

    @XComment("{CMS.MEDIA.VIDEO.ID}")
    private Long videoId;

    @XComment("{CMS.MEDIA.VIDEO.CONTENT_ID}")
    private Long contentId;

    @XComment("{CMS.MEDIA.VIDEO.SITE_ID}")
    private Long siteId;

    @XComment("{CMS.MEDIA.VIDEO.COVER}")
    private String cover;

    @XComment(value = "{CMS.MEDIA.VIDEO.COVER_SRC}", deprecated = true, forRemoval = "1.6.0")
    private String coverSrc;

    @XComment("{CMS.MEDIA.VIDEO.TITLE}")
    private String title;

    @XComment("{CMS.MEDIA.VIDEO.DESC}")
    private String description;

    @XComment("{CMS.MEDIA.VIDEO.TYPE}")
    private String type;

    @XComment("{CMS.MEDIA.VIDEO.PATH}")
    private String path;

    @XComment(value = "{CMS.MEDIA.VIDEO.SRC}", deprecated = true, forRemoval = "1.6.0")
    private String src;

    @XComment("{CMS.MEDIA.VIDEO.FILE_SIZE}")
    private Long fileSize;

    @XComment("{CMS.MEDIA.VIDEO.FORMAT}")
    private String format;

    @XComment("{CMS.MEDIA.VIDEO.DURATION}")
    private Long duration;

    @XComment("{CMS.MEDIA.VIDEO.DECODER}")
    private String decoder;

    @XComment("{CMS.MEDIA.VIDEO.WIDTH}")
    private Integer width;

    @XComment("{CMS.MEDIA.VIDEO.HEIGHT}")
    private Integer height;

    @XComment("{CMS.MEDIA.VIDEO.BIT_RATE}")
    private Integer bitRate;

    @XComment("{CMS.MEDIA.VIDEO.FRAME_RATE}")
    private Integer frameRate;

    @XComment("{CC.ENTITY.SORT}")
    private Integer sortFlag;

    public static TagVideoVO newInstance(CmsVideo video) {
        TagVideoVO vo = new TagVideoVO();
        vo.setVideoId(video.getVideoId());
        vo.setContentId(video.getContentId());
        vo.setSiteId(video.getSiteId());
        vo.setCover(video.getCover());
        vo.setTitle(video.getTitle());
        vo.setDescription(video.getDescription());
        vo.setType(video.getType());
        vo.setPath(video.getPath());
        vo.setFileSize(video.getFileSize());
        vo.setFormat(video.getFormat());
        vo.setDuration(video.getDuration());
        vo.setDecoder(video.getDecoder());
        vo.setWidth(video.getWidth());
        vo.setHeight(video.getHeight());
        vo.setBitRate(video.getBitRate());
        vo.setFrameRate(video.getFrameRate());
        vo.setSortFlag(video.getSortFlag());
        vo.setCreateBy(video.getCreateBy());
        vo.setUpdateBy(video.getUpdateBy());
        vo.setCreateTime(video.getCreateTime());
        vo.setUpdateTime(video.getUpdateTime());
        vo.setRemark(video.getRemark());
        return vo;
    }
}
