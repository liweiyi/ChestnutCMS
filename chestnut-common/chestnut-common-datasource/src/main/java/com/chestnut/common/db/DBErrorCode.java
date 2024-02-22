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
package com.chestnut.common.db;

import com.chestnut.common.exception.ErrorCode;

public enum DBErrorCode implements ErrorCode {
	
	/**
	 * 不支持的数据库类型：{0}
	 */
	UNSUPPORTED_DB_TYPE,

	/**
	 * SQL语句的in方法参数不能为空
	 */
	SQL_IN_PARAMS_EMPTY,

	/**
	 * SQL语句必须指定table
	 */
	SQL_FROM_TABLE;
	
	@Override
	public String value() {
		return "{ERRCODE.DB." + this.name() + "}";
	}
}
