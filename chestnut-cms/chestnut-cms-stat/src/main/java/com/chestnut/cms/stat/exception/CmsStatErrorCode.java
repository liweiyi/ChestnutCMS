package com.chestnut.cms.stat.exception;

import com.chestnut.common.exception.ErrorCode;

public enum CmsStatErrorCode implements ErrorCode {
	
	/**
	 * 请配置百度统计apiKey、secretKey和refreshToken
	 */
	BAIDU_TONGJI_CONFIG_EMPTY;
	
	@Override
	public String value() {
		return "{ERRCODE.CMS.STAT." + this.name() + "}";
	}
}
