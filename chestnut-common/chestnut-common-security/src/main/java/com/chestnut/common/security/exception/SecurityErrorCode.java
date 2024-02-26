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
package com.chestnut.common.security.exception;

import com.chestnut.common.exception.ErrorCode;

public enum SecurityErrorCode implements ErrorCode {
	
	/**
	 * 未登录
	 */
	NOT_LOGIN,
	
	/**
	 * 无访问权限：{0}
	 */
	NOT_PERMISSION,
	
	/**
	 * 演示模式，不允许操作。
	 */
	DEMO_EXCEPTION,
	
	/**
	 * 未知用户类型
	 */
	UNKNOWN_USER_TYPE;
	
	@Override
	public String value() {
		return "{ERRCODE.SECURITY." + this.name() + "}";
	}
}
