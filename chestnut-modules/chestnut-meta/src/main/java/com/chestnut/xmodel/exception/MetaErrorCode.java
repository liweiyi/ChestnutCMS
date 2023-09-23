package com.chestnut.xmodel.exception;

import com.chestnut.common.exception.ErrorCode;

public enum MetaErrorCode implements ErrorCode {
	
	/**
	 * 可用字段数量不足
	 */
	FIELD_LIMIT,
	
	/**
	 * 元数据字段名'0'冲突
	 */
	META_FIELD_CONFLICT,
	
	/**
	 * 指定字段[{0}]不存在
	 */
	DB_FIELD_NOT_EXISTS,
	
	/**
	 * 数据库表‘{0}’已被其他模型使用
	 */
	META_TABLE_CONFICT,
	
	/**
	 * 数据库表‘{0}’不存在
	 */
	META_TABLE_NOT_EXISTS,

	/**
	 * 不支持的元数据模型类型：{0}
	 */
	UNSUPPORTED_META_MODEL_TYPE,

	/**
	 * 元数据模型不存在
	 */
	META_MODEL_NOT_FOUND;
	
	@Override
	public String value() {
		return "{ERRCODE.META." + this.name() + "}";
	}
}
