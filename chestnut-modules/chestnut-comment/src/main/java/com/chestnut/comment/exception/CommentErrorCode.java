package com.chestnut.comment.exception;

import com.chestnut.common.exception.ErrorCode;

public enum CommentErrorCode implements ErrorCode {
	
	/**
	 * 指定评论数据不存在
	 */
	API_COMMENT_NOT_FOUND,
	
	/**
	 * 越权操作。
	 */
	API_ACCESS_DENY;
	
	@Override
	public String value() {
		return "{ERRCODE.COMMENT." + this.name() + "}";
	}
}
