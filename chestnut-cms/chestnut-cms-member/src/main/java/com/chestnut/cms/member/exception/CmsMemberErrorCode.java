package com.chestnut.cms.member.exception;

import com.chestnut.common.exception.ErrorCode;

public enum CmsMemberErrorCode implements ErrorCode {

	/**
	 * 无投稿权限
	 */
	NO_CONTRIBUTE_PRIV;

	@Override
	public String value() {
		return "{ERRCODE.CMS.MEMBER." + this.name() + "}";
	}
}
