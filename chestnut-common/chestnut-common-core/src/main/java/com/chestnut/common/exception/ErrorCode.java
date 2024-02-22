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
package com.chestnut.common.exception;

import com.chestnut.common.domain.R;

public interface ErrorCode {

	/**
	 * 错误信息编码，对应国际化文件key
	 */
	String value();
	
	/**
	 * 错误码
	 */
	default int code() {
		return R.FAIL;
	}

	default GlobalException exception(Object... args) {
		return new GlobalException(this, args);
	}
}
