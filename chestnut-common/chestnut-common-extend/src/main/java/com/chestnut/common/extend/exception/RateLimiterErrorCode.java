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
package com.chestnut.common.extend.exception;

import com.chestnut.common.exception.ErrorCode;

public enum RateLimiterErrorCode implements ErrorCode {
	
	/**
	 * Lang: 访问过于频繁，请稍候再试
	 */
	RATE_LIMIT,
	
	/**
	 * Lang: 服务器限流异常，请稍候再试
	 */
	RATE_LIMIT_ERR;
	
	@Override
	public String value() {
		return "{ERRCODE.EXTEND." + this.name() + "}";
	}
}
