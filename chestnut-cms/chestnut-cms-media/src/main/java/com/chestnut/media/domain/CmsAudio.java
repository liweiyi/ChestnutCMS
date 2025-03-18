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
 * 音频数据表对象 [cms_audio]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsAudio.TABLE_NAME)
public class CmsAudio extends BaseEntity implements IBackupable<BCmsAudio> {

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_audio";

    @TableId(value = "audio_id", type = IdType.INPUT)
    private Long audioId;

    /**
     * 所属内容ID
     */
    private Long contentId;
    
    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 音频标题
     */
    private String title;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 简介
     */
    private String description;

    /**
     * 音频类型
     */
    private String type;

    /**
     * 音频相对站点资源目录路径
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
     * 音频格式
     */
    private String format;

    /**
     * 音频时长，单位：毫秒
     */
    private Long duration;
    
    /**
     * 编码方式
     */
    private String decoder;
    
    /**
     * 声道数
     */
    private Integer channels;
    
    /**
     * 比特率
     */
    private Integer bitRate;
    
    /**
     * 采样率
     */
    private Integer samplingRate;

    /**
     * 排序字段
     */
    private Integer sortFlag;

    @Override
    public BCmsAudio toBackupEntity() {
        BCmsAudio backupEntity = new BCmsAudio();
        BeanUtils.copyProperties(this, backupEntity);
        return backupEntity;
    }
}
