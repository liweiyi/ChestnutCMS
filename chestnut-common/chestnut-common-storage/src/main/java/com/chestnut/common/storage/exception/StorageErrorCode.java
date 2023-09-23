package com.chestnut.common.storage.exception;

import com.chestnut.common.exception.ErrorCode;

public enum StorageErrorCode implements ErrorCode {
	
	/**
	 * 不支持的存储方式：{0}
	 */
	UNSUPPORTED_STORAGE_TYPE;
	
	@Override
	public String value() {
		return "{ERRCODE.STORAGE." + this.name() + "}";
	}
}
