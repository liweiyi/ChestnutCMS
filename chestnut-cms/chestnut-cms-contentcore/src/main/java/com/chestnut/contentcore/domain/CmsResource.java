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
package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.system.fixed.dict.EnableOrDisable;

import lombok.Getter;
import lombok.Setter;

/**
 * 资源表对象 [cms_resource]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsResource.TABLE_NAME)
public class CmsResource extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_resource";

    /**
     * 资源ID
     */
    @TableId(value = "resource_id", type = IdType.INPUT)
    private Long resourceId;

    /**
     * 站点id
     */
    private Long siteId;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源类型名称
     */
    @TableField(exist = false)
    private String resourceTypeName;
    
    /**
     * 存储类型，默认：local
     */
    private String storageType;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源路径（相对站点根目录路径）
     */
    private String path;

    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 后缀名，不带.
     */
    private String suffix;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 文件大小
     */
    private Long fileSize;
    
    @TableField(exist = false)
    private String fileSizeName;

    /**
     * 来源地址
     */
    private String sourceUrl;

    /**
     * 状态
     */
    private String status;

    /**
     * 引用关系
     */
    private String usageInfo;
    
    @TableField(exist = false)
    private String src;
    
    @TableField(exist = false)
    private String internalUrl;
    
    public boolean isEnable() {
    	return EnableOrDisable.isEnable(this.status);
    }
}
