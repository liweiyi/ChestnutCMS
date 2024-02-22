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
package com.chestnut.xmodel.exception;

import com.chestnut.common.exception.ErrorCode;

public enum MetaErrorCode implements ErrorCode {
	
	/**
	 * 可用字段数量不足
	 */
	FIELD_LIMIT,
	
	/**
	 * 元数据字段名'0'冲突
	 */
	META_FIELD_CONFLICT,
	
	/**
	 * 指定字段[{0}]不存在
	 */
	DB_FIELD_NOT_EXISTS,
	
	/**
	 * 数据库表‘{0}’已被其他模型使用
	 */
	META_TABLE_CONFICT,
	
	/**
	 * 数据库表‘{0}’不存在
	 */
	META_TABLE_NOT_EXISTS,

	/**
	 * 不支持的元数据模型类型：{0}
	 */
	UNSUPPORTED_META_MODEL_TYPE,

	/**
	 * 元数据模型不存在
	 */
	META_MODEL_NOT_FOUND;
	
	@Override
	public String value() {
		return "{ERRCODE.META." + this.name() + "}";
	}
}
