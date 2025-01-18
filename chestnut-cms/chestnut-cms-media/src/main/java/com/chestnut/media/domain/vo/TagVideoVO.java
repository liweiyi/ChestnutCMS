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

    @XComment("视频ID")
    private Long videoId;

    @XComment("所属内容ID")
    private Long contentId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("视频封面图")
    private String cover;

    @XComment(value = "视频封面图", deprecated = true, forRemoval = "1.6.0")
    private String coverSrc;

    @XComment("视频标题")
    private String title;

    @XComment("简介")
    private String description;

    @XComment("视频类型")
    private String type;

    @XComment("视频路径，如果type==SHARE则存放第三方引用代码")
    private String path;

    @XComment(value = "预览路径", deprecated = true, forRemoval = "1.6.0")
    private String src;

    @XComment("文件大小")
    private Long fileSize;

    @XComment("视频格式")
    private String format;

    @XComment("时长，单位：毫秒")
    private Long duration;

    @XComment("编码方式")
    private String decoder;

    @XComment("视频宽度")
    private Integer width;

    @XComment("视频高度")
    private Integer height;

    @XComment("比特率")
    private Integer bitRate;

    @XComment("帧率")
    private Integer frameRate;

    @XComment("排序值")
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
