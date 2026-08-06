/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.message.exception;

import com.chestnut.common.exception.ErrorCode;

public enum MessageErrorCode implements ErrorCode {
	
	/**
	 * 不支持的消息类型：{0}
	 */
	UNSUPPORTED_MESSAGE_TYPE,

    /**
     * 不支持的短信供应商：{0}
     */
    UNSUPPORTED_SMS_PROVIDER,

    /**
     * 发送消息失败：{0}
     */
    SEND_MESSAGE_FAIL,

    /**
     * 不支持的用户消息类型：{0}
     */
    UNSUPPORTED_USER_MESSAGE_TYPE,

    INVALID_USER_MESSAGE_REQUEST,

    INVALID_BUSINESS_SCENE,

    INVALID_MESSAGE_RECEIVER,

    INVALID_RATE_LIMIT_CONFIG,

    MESSAGE_CONFIG_NOT_FOUND,

    MESSAGE_TEMPLATE_NOT_FOUND,

    MESSAGE_TYPE_MISMATCH,

    MESSAGE_RATE_LIMIT,

    MESSAGE_RATE_LIMIT_ERROR,

    /**
     * 消息蒙版模板ID不能为空
     */
    TEMPLATE_ID_NOT_EMPTY;
	
	@Override
	public String value() {
		return "{ERR.MSG." + this.name() + "}";
	}
}
