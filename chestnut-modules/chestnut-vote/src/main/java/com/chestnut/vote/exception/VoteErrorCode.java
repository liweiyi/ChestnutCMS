/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.vote.exception;

import com.chestnut.common.exception.ErrorCode;

public enum VoteErrorCode implements ErrorCode {
	
	/**
	 * 不支持的问卷调查用户类型：{0}
	 */
	UNSUPPORTED_VOTE_USER_TYPE,

	/**
	 * 不支持的问卷调查选项类型：{0}
	 */
	UNSUPPORTED_VOTE_ITEM_TYPE,
	
	/**
	 * 超出问卷调查参与次数上限
	 */
	VOTE_TOTAL_LIMIT,
	
	/**
	 * 超出问卷调查每日参与次数上限
	 */
	VOTE_DAY_LIMIT,
	
	/**
	 * 问卷调查还未开始或已过期
	 */
	TIME_ERR;
	
	@Override
	public String value() {
		return "{ERRCODE.VOTE." + this.name() + "}";
	}
}
