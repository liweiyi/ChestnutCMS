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
package com.chestnut.common.domain;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 响应信息主体
 */
public class R<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** 成功 */
	public static final int SUCCESS = HttpStatus.OK.value();

	/** 失败 */
	public static final int FAIL = HttpStatus.INTERNAL_SERVER_ERROR.value();

	/** 成功默认消息 */
	public static final String SUCCESS_MESSAGE = "SUCCESS";

	/** 失败默认消息 */
	public static final String FAIL_MESSAGE = "FAIL";

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
		return restResult(null, SUCCESS, SUCCESS_MESSAGE);
	}

	public static <T> R<T> ok(T data) {
		return restResult(data, SUCCESS, SUCCESS_MESSAGE);
	}

	public static <T> R<T> ok(T data, String msg) {
		return restResult(data, SUCCESS, msg);
	}

	public static <T> R<T> ok(int code, T data) {
		return restResult(data, code, SUCCESS_MESSAGE);
	}

	public static <T> R<T> fail() {
		return restResult(null, FAIL, FAIL_MESSAGE);
	}

	public static <T> R<T> fail(String msg) {
		return restResult(null, FAIL, msg);
	}

	public static <T> R<T> fail(T data) {
		return restResult(data, FAIL, FAIL_MESSAGE);
	}

	public static <T> R<T> fail(T data, String msg) {
		return restResult(data, FAIL, msg);
	}

	public static <T> R<T> fail(int code, String msg) {
		return restResult(null, code, msg);
	}

	private static <T> R<T> restResult(T data, int code, String msg) {
		R<T> apiResult = new R<>();
		apiResult.setCode(code);
		apiResult.setData(data);
		apiResult.setMsg(msg);
		return apiResult;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
