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
package com.chestnut.search.exception;

import com.chestnut.common.exception.ErrorCode;

public enum SearchErrorCode implements ErrorCode {

	/**
	 * 字典词“{0}”已存在
	 */
	DICT_WORD_EXISTS,

	/**
	 * 索引模型“{0}”不存在
	 */
	MODEL_NOT_EXISTS,

	/**
	 * 不支持的检索类型：{0}
	 */
	UNSUPPORTED_SEARCH_TYPE,

	/**
	 * ElasticSearch连接失败，请检查连接配置和ES服务是否有效。
	 */
	ESConnectFail;

	@Override
	public String value() {
		return "{ERRCODE.SEARCH." + this.name() + "}";
	}
}
