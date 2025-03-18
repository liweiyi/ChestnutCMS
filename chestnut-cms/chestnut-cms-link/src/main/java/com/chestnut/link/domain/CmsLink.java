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
package com.chestnut.link.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
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

    @XComment("友链ID")
    @TableId(value = "link_id", type = IdType.INPUT)
    private Long linkId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("所属分组ID")
    private Long groupId;

    @XComment("链接名称")
    private String name;

    @XComment("链接URL")
    private String url;

    @XComment("图片路径")
    private String logo;

    @TableField(exist = false)
    @XComment(value = "图片访问地址", deprecated = true, forRemoval = "1.6.0")
    private String src;
    
    @XComment("排序值")
    private Long sortFlag;
}
