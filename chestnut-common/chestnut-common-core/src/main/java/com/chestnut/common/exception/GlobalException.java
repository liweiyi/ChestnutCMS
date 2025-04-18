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
package com.chestnut.common.exception;

import com.chestnut.common.i18n.I18nUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;

/**
 * 自定义全局异常
 */
@Getter
@NoArgsConstructor
public class GlobalException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private ErrorCode errorCode = CommonErrorCode.UNKNOWN_ERROR;

	private Object[] errArgs;
	
	public GlobalException(ErrorCode errCode, Object... errArgs) {
		super(I18nUtils.get(errCode.value(), LocaleContextHolder.getLocale(), errArgs));
		this.errorCode = errCode;
		this.errArgs = errArgs;
	}

	public GlobalException(Throwable cause, ErrorCode errCode, Object... errArgs) {
		super(I18nUtils.get(errCode.value(), LocaleContextHolder.getLocale(), errArgs), cause);
		this.errorCode = errCode;
		this.errArgs = errArgs;
	}

	public GlobalException(String message) {
		super(message);
	}
}