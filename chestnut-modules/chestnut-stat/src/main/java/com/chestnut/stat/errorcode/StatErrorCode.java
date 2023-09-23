package com.chestnut.stat.errorcode;

import com.chestnut.common.exception.ErrorCode;

public enum StatErrorCode implements ErrorCode {
	
	/**
	 * 不支持的统计类型：{0}
	 */
	UNSUPPORT_STAT_TYPE,
	
	/**
	 * 不支持的统计事件处理器类型
	 */
	UNSUPPORT_STAT_EVENT_HANDLER;

	@Override
	public String value() {
		return "{ERRCODE.STAT." + this.name() + "}";
	}
}
