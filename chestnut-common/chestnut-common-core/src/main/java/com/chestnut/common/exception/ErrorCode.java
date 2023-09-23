package com.chestnut.common.exception;

import com.chestnut.common.domain.R;

public interface ErrorCode {

	/**
	 * 错误信息编码，对应国际化文件key
	 */
	public String value();
	
	/**
	 * 错误码
	 */
	default public int code() {
		return R.FAIL;
	}

	default public GlobalException exception(Object... args) {
		return new GlobalException(this, args);
	}
}
