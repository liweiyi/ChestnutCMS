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
package com.chestnut.word.exception;

import com.chestnut.common.exception.ErrorCode;

public enum WordErrorCode implements ErrorCode {
	

	/**
	 * 易错词“{0}”已存在
	 */
	CONFLIECT_ERROR_PRONE_WORD,
	
	/**
	 * 热词分組编码冲突
	 */
	CONFLIECT_HOT_WORD_GROUP,

	/**
	 * 热词“{0}”已存在
	 */
	CONFLIECT_HOT_WORD,
	
	/**
	 * 敏感词“{0}”已存在
	 */
	CONFLIECT_SENSITIVE_WORD,
	
	/**
	 * TAG词“{0}”已存在
	 */
	CONFLIECT_TAG_WORD;
	
	@Override
	public String value() {
		return "{ERRCODE.WORD." + this.name() + "}";
	}
}
