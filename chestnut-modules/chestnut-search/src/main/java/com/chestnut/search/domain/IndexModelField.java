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
package com.chestnut.search.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 索引模型字段表
 */
@Getter
@Setter
@TableName(value = IndexModelField.TABLE_NAME)
public class IndexModelField extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "search_index_model_field";

	/*
	 * 模型字段ID
	 */
	@TableId(value = "field_id", type = IdType.INPUT)
    private Long fieldId;

	/*
	 * 所属模型ID 
	 */
    private Long modelId;
	
	/*
	 * 字段标签
	 */
	private String fieldLabel;
	
	/*
	 * 字段名
	 */
	private String fieldName;
	
	/*
	 * 字段类型
	 */
	private String fieldType;
	
	/*
	 * 是否主键
	 */
	private String primaryKey;
	
	/*
	 * 字段权重
	 */
	private Double weight;
	
	/*
	 * 是否分词
	 */
	private String index;
	
	/*
	 * 分词策略
	 */
	private String analyzer;
}
