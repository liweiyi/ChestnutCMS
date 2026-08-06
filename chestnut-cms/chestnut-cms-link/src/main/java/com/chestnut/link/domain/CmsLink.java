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
package com.chestnut.link.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

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

    @Serial
    private static final long serialVersionUID=1L;

	public static final String TABLE_NAME = "cms_link";

    @XComment("{CMS.LINK.ID}")
    @TableId(value = "link_id", type = IdType.INPUT)
    private Long linkId;

    @XComment("{CMS.LINK.SITE_ID}")
    private Long siteId;

    @XComment("{CMS.LINK.GROUP_ID}")
    private Long groupId;

    @XComment("{CMS.LINK.NAME}")
    private String name;

    @XComment("{CMS.LINK.URL}")
    private String url;

    @XComment("{CMS.LINK.LOGO}")
    private String logo;

    @TableField(exist = false)
    @XComment(value = "{CMS.LINK.SRC}", deprecated = true, forRemoval = "1.6.0")
    private String src;
    
    @XComment("{CC.ENTITY.SORT}")
    private Long sortFlag;
}
