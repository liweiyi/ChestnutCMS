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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 模板表对象 [cms_template]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsTemplate.TABLE_NAME)
public class CmsTemplate extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_template";

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.INPUT)
    private Long templateId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 发布通道编码
     */
    private String publishPipeCode;

    /**
     * 模板文件路径
     */
    private String path;
    
    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板文件大小
     */
    private Long filesize;
    
    /**
     * 模板文件最后变更时间
     */
    private Long modifyTime;
}
