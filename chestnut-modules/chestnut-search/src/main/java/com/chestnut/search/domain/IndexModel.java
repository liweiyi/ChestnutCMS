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

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 索引模型
 */
@Getter
@Setter
@TableName(value = IndexModel.TABLE_NAME)
public class IndexModel extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "search_index_model";

	/*
	 * 模型主键ID
	 */
	@TableId(value = "model_id", type = IdType.INPUT)
    private Long modelId;
	
	/*
	 * 搜索类型
	 */
	private String type;
	
	/*
	 * 模型名称
	 */
	private String name;
	
	/*
	 * 模型唯一标识编码（索引名）
	 */
	private String code;
	
	/*
	 * 模型字段列表
	 */
	@TableField(exist = false)
	private List<IndexModelField> fields;
}
