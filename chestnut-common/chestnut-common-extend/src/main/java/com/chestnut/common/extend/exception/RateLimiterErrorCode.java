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
