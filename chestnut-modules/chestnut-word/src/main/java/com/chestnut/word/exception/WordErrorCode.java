package com.chestnut.word.exception;

import com.chestnut.common.exception.ErrorCode;

public enum WordErrorCode implements ErrorCode {
	

	/**
	 * 易错词“{0}”已存在
	 */
	CONFLIECT_ERROR_PRONE_WORD,
	
	/**
	 * 热词分組名称/编码冲突
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
