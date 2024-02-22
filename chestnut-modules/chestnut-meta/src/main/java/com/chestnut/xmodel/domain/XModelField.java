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
package com.chestnut.xmodel.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.xmodel.dto.FieldOptions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


/**
 * 元数据模型字段定义表[XModelField]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = XModelField.TABLE_NAME, autoResultMap = true)
public class XModelField extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "x_model_field";

    @TableId(value = "field_id", type = IdType.INPUT)
    private Long fieldId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 名称
     */
    private String name;

    /**
     * 唯一标识编码
     */
    private String code;

    /**
     * 如果是x_model_data表，字段类型：varchar(50)，varchar(200)，varchar(2000)，mediumText，bigint，double，datetime
     */
    private String fieldType;
    
    /**
     * 如果是自定义表，对应数据库表字段名
     */
    private String fieldName;

    /**
     * 对应前端显示控件类型
     */
    private String controlType;
    
    /**
     * 可选项
     * 格式：
     * 	{ 'type':'dict','value': "dict_type"}<br/>
     * { 'type':'text','value': "v1=label1\nv2=label2\n..."}<br/>
     * type: <br/>
     * 	 dict = 数据字典，例如：cms_content_status<br/>
     *   text = 自定义格式：<名称=值>\n...
     *   	
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FieldOptions options;
    
    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 校验规则
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> validations;

    /**
     * 排序字段
     */
    private Long sortFlag;
    
    /**
     * 字段值
     */
    @TableField(exist = false)
    private String value;
}
