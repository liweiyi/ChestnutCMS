package com.chestnut.common.security.exception;

import com.chestnut.common.exception.ErrorCode;

public enum SecurityErrorCode implements ErrorCode {
	
	/**
	 * 未登录
	 */
	NOT_LOGIN,
	
	/**
	 * 无访问权限
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
