/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.customform.exception;

import com.chestnut.common.exception.ErrorCode;

public enum CustomFormErrorCode implements ErrorCode {
	
	/**
	 * 表单不存在
	 */
	FORM_NOT_FOUND,
	
	/**
	 * uuid不能为空
	 */
	MISSING_UUID,

	/**
	 * 未登录
	 */
	NOT_LOGIN,

	/**
	 * 不能重复提交
	 */
	CANNOT_RESUBMIT;
	
	@Override
	public String value() {
		return "{ERR.CUSTOM_FORM." + this.name() + "}";
	}
}
