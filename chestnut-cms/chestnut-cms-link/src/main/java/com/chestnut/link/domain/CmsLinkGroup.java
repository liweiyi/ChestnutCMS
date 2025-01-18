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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 友情链接分组表对象 [cms_link_group]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsLinkGroup.TABLE_NAME)
public class CmsLinkGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cms_link_group";

    @XComment("友链分组ID")
    @TableId(value = "link_group_id", type = IdType.INPUT)
    private Long linkGroupId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("分组名称")
    private String name;

    @XComment("分组编码")
    private String code;
    
    @XComment("排序值")
    private Long sortFlag;
}
