package com.chestnut.common.db;

import com.chestnut.common.exception.ErrorCode;

public enum DBErrorCode implements ErrorCode {
	
	/**
	 * 不支持的数据库类型：{0}
	 */
	UNSUPPORTED_DB_TYPE,

	/**
	 * SQL语句的in方法参数不能为空
	 */
	SQL_IN_PARAMS_EMPTY,

	/**
	 * SQL语句必须指定table
	 */
	SQL_FROM_TABLE;
	
	@Override
	public String value() {
		return "{ERRCODE.DB." + this.name() + "}";
	}
}
