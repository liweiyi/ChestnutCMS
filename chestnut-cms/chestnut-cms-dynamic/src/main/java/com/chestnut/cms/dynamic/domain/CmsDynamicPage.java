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
package com.chestnut.cms.dynamic.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * 自定义动态页面表对象 [cms_dynamic_page]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CmsDynamicPage.TABLE_NAME, autoResultMap = true)
public class CmsDynamicPage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_dynamic_page";

    @TableId(value = "page_id", type = IdType.INPUT)
    private Long pageId;

    private Long siteId;

    /**
     * 名称
     */
    private String name;

    /**
     * 别名，唯一标识
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * 访问路径
     */
    private String path;

    /**
     * 初始化数据类型集合
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> initDataTypes;

    /**
     * 发布通道模板
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> templates;
}
