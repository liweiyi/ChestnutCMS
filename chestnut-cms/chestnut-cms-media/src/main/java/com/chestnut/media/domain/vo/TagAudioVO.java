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

    @XComment("音频ID")
    private Long audioId;

    @XComment("所属内容ID")
    private Long contentId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("音频标题")
    private String title;

    @XComment("作者")
    private String author;

    @XComment("简介")
    private String description;

    @XComment("音频类型")
    private String type;

    @XComment("音频路径")
    private String path;

    @XComment(value = "预览路径", deprecated = true, forRemoval = "1.6.0")
    private String src;

    @XComment("文件大小，单位：字节")
    private Long fileSize;

    @XComment("格式")
    private String format;

    @XComment("时长，单位：毫秒")
    private Long duration;

    @XComment("编码方式")
    private String decoder;

    @XComment("声道数")
    private Integer channels;

    @XComment("比特率")
    private Integer bitRate;

    @XComment("采样率")
    private Integer samplingRate;

    @XComment("排序值")
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
