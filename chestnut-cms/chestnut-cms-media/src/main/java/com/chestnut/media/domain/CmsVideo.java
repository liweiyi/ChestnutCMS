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
package com.chestnut.media.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.IBackupable;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serial;

/**
 * 视频数据表对象 [cms_video]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsVideo.TABLE_NAME)
public class CmsVideo extends BaseEntity implements IBackupable<BCmsVideo> {

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_video";

    @TableId(value = "video_id", type = IdType.INPUT)
    private Long videoId;

    /**
     * 所属内容ID
     */
    private Long contentId;
    
    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 视频封面图
     */
    private String cover;

    /**
     * 视频封面图预览路径
     */
    @TableField(exist = false)
    private String coverSrc;

    /**
     * 视频标题
     */
    private String title;
    
    /**
     * 简介
     */
    private String description;

    /**
     * 视频类型
     */
    private String type;

    /**
     * 视频相对站点资源目录路径，如果type==SHARE则存放第三方引用代码
     */
    private String path;

    /**
     * 预览路径
     */
    @TableField(exist = false)
    private String src;

    /**
     * 音频文件大小
     */
    private Long fileSize;
    
    @TableField(exist = false)
    private String fileSizeName;
    
    /**
     * 视频格式
     */
    private String format;

    /**
     * 视频时长，单位：毫秒
     */
    private Long duration;
    
    /**
     * 编码方式
     */
    private String decoder;
    
    /**
     * 视频宽
     */
    private Integer width;
    
    /**
     * 视频高
     */
    private Integer height;
    
    /**
     * 比特率（bsp）
     */
    private Integer bitRate;
    
    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 排序字段
     */
    private Integer sortFlag;

    @Override
    public BCmsVideo toBackupEntity() {
        BCmsVideo backupEntity = new BCmsVideo();
        BeanUtils.copyProperties(this, backupEntity);
        return backupEntity;
    }
}
