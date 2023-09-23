package com.chestnut.common.exception;

import org.springframework.context.i18n.LocaleContextHolder;

import com.chestnut.common.i18n.I18nUtils;

import lombok.NoArgsConstructor;

/**
 * 自定义全局异常
 */
@NoArgsConstructor
public class GlobalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private ErrorCode errorCode = CommonErrorCode.UNKNOWN_ERROR;

	private Object[] errArgs;
	

	public GlobalException(ErrorCode errCode, Object... errArgs) {
		super(I18nUtils.get(errCode.value(), LocaleContextHolder.getLocale(), errArgs));
		this.errorCode = errCode;
		this.errArgs = errArgs;
	}

	public GlobalException(String message) {
		super(message);
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}

	public Object[] getErrArgs() {
		return this.errArgs;
	}
}