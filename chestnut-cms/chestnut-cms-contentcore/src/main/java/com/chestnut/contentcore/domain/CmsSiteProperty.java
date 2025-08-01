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
package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 站点自定义属性表对象 [cms_site_property]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsSiteProperty.TABLE_NAME)
public class CmsSiteProperty extends BaseEntity {

    @Serial
    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_site_property";

    @TableId(value = "property_id", type = IdType.INPUT)
    @XComment("ID")
    private Long propertyId;

    @XComment("所属站点ID")
    private Long siteId;

    @XComment("属性名称")
    @NotBlank
    private String propName;

    @XComment("属性编码")
    @Pattern(regexp = "[A-Za-z0-9_]+", message = "{VALIDATOR.CMS.SITE_PROPERTY.REGEXP_ERR}")
    private String propCode;

    @XComment("属性值")
    private String propValue;
}
