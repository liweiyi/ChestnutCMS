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
package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 页面部件表对象 [cms_page_widget]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */

@Getter
@Setter
@TableName(value = CmsPageWidget.TABLE_NAME, autoResultMap = true)
public class CmsPageWidget extends BaseEntity {

	private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_page_widget";

	/**
	 * 页面部件ID
	 */
	@TableId(value = "page_widget_id", type = IdType.INPUT)
    private Long pageWidgetId;
	
	/**
	 * 所属站点ID
	 */
	private Long siteId;
	
	/**
	 * 所属栏目ID
	 */
	private Long catalogId;

    /**
     * 所属栏目祖级IDs
     */
    private String catalogAncestors;

	/**
	 * 类型
	 */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;
    
    /**
     * 状态
     */
    private String state;

    /**
     * 发布通道
     */
    @Deprecated(since = "1.5.6", forRemoval = true)
    private String publishPipeCode;

    /**
     * 模板路径
     */
    @Deprecated(since = "1.5.6", forRemoval = true)
    private String template;

    /**
     * 发布通道模板配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> templates;
    
    /**
     * 静态化目录
     */
    private String path;
    
    /**
     * 内容
     */
    private String content;
    
    @TableField(exist = false)
    private Object contentObj;

    public String getTemplate(String publishPipeCode) {
        String templatePath = MapUtils.getString(templates, publishPipeCode);
        // 兼容历史版本数据
        if (StringUtils.isEmpty(templatePath) && StringUtils.equals(publishPipeCode, this.publishPipeCode)) {
            templatePath = this.template;
        }
        return templatePath;
    }
}
