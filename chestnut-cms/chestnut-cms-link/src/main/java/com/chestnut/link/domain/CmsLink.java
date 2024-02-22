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
package com.chestnut.link.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 友情链接表对象 [cms_link]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsLink.TABLE_NAME)
public class CmsLink extends BaseEntity {

    private static final long serialVersionUID=1L;

	public static final String TABLE_NAME = "cms_link";

    @TableId(value = "link_id", type = IdType.INPUT)
    private Long linkId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 所属分组ID
     */
    private Long groupId;

    /**
     * 链接名称
     */
    private String name;

    /**
     * 链接URL
     */
    private String url;

    /**
     * 图片路径
     */
    private String logo;

    /**
     * 图片预览路径
     */
    @TableField(exist = false)
    private String src;
    
    /**
     * 排序标识
     */
    private Long sortFlag;
}
