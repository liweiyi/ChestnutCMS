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
import com.chestnut.media.domain.CmsAudio;
import lombok.Getter;
import lombok.Setter;

/**
 * TagAudioVO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagAudioVO extends TagBaseVO {

    @XComment("{CMS.MEDIA.AUDIO.ID}")
    private Long audioId;

    @XComment("{CMS.MEDIA.AUDIO.CONTENT_ID}")
    private Long contentId;

    @XComment("{CMS.MEDIA.AUDIO.SITE_ID}")
    private Long siteId;

    @XComment("{CMS.MEDIA.AUDIO.TITLE}")
    private String title;

    @XComment("{CMS.MEDIA.AUDIO.AUTHOR}")
    private String author;

    @XComment("{CMS.MEDIA.AUDIO.DESC}")
    private String description;

    @XComment("{CMS.MEDIA.AUDIO.TYPE}")
    private String type;

    @XComment("{CMS.MEDIA.AUDIO.PATH}")
    private String path;

    @XComment(value = "{CMS.MEDIA.AUDIO.SRC}", deprecated = true, forRemoval = "1.6.0")
    private String src;

    @XComment("{CMS.MEDIA.AUDIO.FILE_SIZE}")
    private Long fileSize;

    @XComment("{CMS.MEDIA.AUDIO.FORMAT}")
    private String format;

    @XComment("{CMS.MEDIA.AUDIO.DURATION}")
    private Long duration;

    @XComment("{CMS.MEDIA.AUDIO.DECODER}")
    private String decoder;

    @XComment("{CMS.MEDIA.AUDIO.CHANNELS}")
    private Integer channels;

    @XComment("{CMS.MEDIA.AUDIO.BIT_RATE}")
    private Integer bitRate;

    @XComment("{CMS.MEDIA.AUDIO.SAMPLING_RATE}")
    private Integer samplingRate;

    @XComment("{CC.ENTITY.SORT}")
    private Integer sortFlag;

    public static TagAudioVO newInstance(CmsAudio audio) {
        TagAudioVO vo = new TagAudioVO();
        vo.setAudioId(audio.getAudioId());
        vo.setContentId(audio.getContentId());
        vo.setSiteId(audio.getSiteId());
        vo.setTitle(audio.getTitle());
        vo.setAuthor(audio.getAuthor());
        vo.setDescription(audio.getDescription());
        vo.setType(audio.getType());
        vo.setPath(audio.getPath());
        vo.setFileSize(audio.getFileSize());
        vo.setFormat(audio.getFormat());
        vo.setDuration(audio.getDuration());
        vo.setDecoder(audio.getDecoder());
        vo.setChannels(audio.getChannels());
        vo.setBitRate(audio.getBitRate());
        vo.setSamplingRate(audio.getSamplingRate());
        vo.setSortFlag(audio.getSortFlag());
        vo.setCreateBy(audio.getCreateBy());
        vo.setUpdateBy(audio.getUpdateBy());
        vo.setCreateTime(audio.getCreateTime());
        vo.setUpdateTime(audio.getUpdateTime());
        vo.setRemark(audio.getRemark());
        return vo;
    }
}
