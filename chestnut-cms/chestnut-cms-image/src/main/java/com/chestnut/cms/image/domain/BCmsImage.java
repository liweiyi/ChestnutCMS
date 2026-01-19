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
package com.chestnut.cms.image.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.DBConstants;
import com.chestnut.common.db.domain.BackupBaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serial;

/**
 * 图集图片备份表对象 [cms_image]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(BCmsImage.TABLE_NAME)
public class BCmsImage extends BackupBaseEntity<CmsImage> {

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = DBConstants.BACKUP_TABLE_PREFIX + CmsImage.TABLE_NAME;

    private Long imageId;

    /**
     * 所属内容ID
     */
    private Long contentId;
    
    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 图片标题
     */
    private String title;

    /**
     * 图片摘要
     */
    private String description;

    /**
     * 图片原文件名
     */
    private String fileName;

    /**
     * 图片路径
     */
    private String path;

    /**
     * 图片路径
     */
    @TableField(exist = false)
    private String src;

    /**
     * 图片类型
     */
    private String imageType;
    
    /**
     * 图片大小
     */
    private Long fileSize;
    
    @TableField(exist = false)
    private String fileSizeName;

    /**
     * 图片宽
     */
    private Integer width;

    /**
     * 图片高
     */
    private Integer height;

    /**
     * 跳转链接
     */
    private String redirectUrl;

    /**
     * 图片点击量
     */
    private Integer hitCount;

    /**
     * 排序字段
     */
    private Integer sortFlag;

    @Override
    public CmsImage toSourceEntity() {
        CmsImage entity = new CmsImage();
        BeanUtils.copyProperties(this, entity);
        return entity;
    }
}
