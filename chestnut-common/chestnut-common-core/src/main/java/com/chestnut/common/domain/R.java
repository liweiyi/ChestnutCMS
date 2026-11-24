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
package com.chestnut.common.domain;

import com.chestnut.common.exception.CommonTips;
import com.chestnut.common.exception.TipMessage;
import com.chestnut.common.i18n.I18nUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class R<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/** 成功 */
	public static final int SUCCESS = HttpStatus.OK.value();

	/** 失败 */
	public static final int FAIL = HttpStatus.INTERNAL_SERVER_ERROR.value();

	/** 成功默认消息 */
	public static final String SUCCESS_MESSAGE = "{CC.COMMON.R.SUCCESS}";

	/** 失败默认消息 */
	public static final String FAIL_MESSAGE = "{CC.COMMON.R.FAIL}";

	/**
	 * 响应码
	 */
	private int code;

	/**
	 * 响应消息
	 */
	private String msg;

	/**
	 * 响应数据主体
	 */
	private T data;

	@JsonIgnore
	public boolean isSuccess() {
		return this.code == SUCCESS;
	}

	public static <T> R<T> ok() {
		return ok(null);
	}

	public static <T> R<T> ok(T data) {
		return okWithTip(data, CommonTips.OPERATION_SUCCEEDED);
	}

	public static <T> R<T> ok(T data, String msg) {
		return restResult(data, SUCCESS, I18nUtils.get(msg));
	}

	public static <T> R<T> ok(int code, T data) {
		return restResult(data, code, CommonTips.OPERATION_SUCCEEDED.locale());
	}

	public static <T> R<T> okWithTip(T data, TipMessage tip, Object... args) {
		return restResult(data, SUCCESS, tip.locale(args));
	}

	public static <T> R<T> fail() {
		return failWithTip(CommonTips.OPERATION_FAILED);
	}

	public static <T> R<T> fail(String msg) {
		return failWithMessage(I18nUtils.get(msg));
	}

	public static <T> R<T> fail(T data) {
		return restResult(data, FAIL, CommonTips.OPERATION_FAILED.locale());
	}

	public static <T> R<T> fail(T data, String msg) {
		return restResult(data, FAIL, I18nUtils.get(msg));
	}

	public static <T> R<T> fail(int code, String msg) {
		return restResult(null, code, I18nUtils.get(msg));
	}

	public static <T> R<T> failWithTip(TipMessage tip, Object... args) {
		return failWithTip(FAIL, tip, args);
	}

	public static <T> R<T> failWithTip(int code, TipMessage tip, Object... args) {
		return restResult(null, code, tip.locale(args));
	}

	/** 返回已经解析的提示文本，保留消息参数中的花括号。 */
	public static <T> R<T> failWithMessage(String message) {
		return restResult(null, FAIL, message);
	}

	private static <T> R<T> restResult(T data, int code, String msg) {
		R<T> apiResult = new R<>();
		apiResult.setCode(code);
		apiResult.setData(data);
		apiResult.setMsg(msg);
		return apiResult;
	}
}
