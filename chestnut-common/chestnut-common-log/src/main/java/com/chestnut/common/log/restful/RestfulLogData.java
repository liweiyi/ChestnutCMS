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
package com.chestnut.common.log.restful;

import java.io.Serializable;
import java.util.LinkedHashMap;

import org.apache.commons.collections4.MapUtils;

import com.chestnut.common.utils.StringUtils;

public class RestfulLogData extends LinkedHashMap<String, Object> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 请求地址
	 */
	public static final String PARAM_REQUEST_URI = "request_uri";
	
	/**
	 * 请求方式，@see org.springframework.web.bind.annotation.RequestMethod
	 */
	public static final String PARAM_REQUEST_METHOD = "request_method";
	
	/**
	 * 请求参数
	 */
	public static final String PARAM_REQUEST_ARGS = "request_args";
	
	/**
	 * 请求方法
	 */
	public static final String PARAM_REQUEST_FUNCTION = "request_function";
	
	/**
	 * 返回结果Code
	 */
	public static final String PARAM_RESPONSE_CODE = "response_code";

	/**
	 * 返回结果
	 */
	public static final String PARAM_RESPONSE_RESULT = "response_result";

	/**
	 * IP
	 */
	public static final String PARAM_IP = "ip";

	/**
	 * 浏览器用户代理信息
	 */
	public static final String PARAM_USER_AGENT = "user_agent";

	/**
	 * 用户ID
	 */
	public static final String PARAM_USER_ID = "user_id";

	/**
	 * 用户类型
	 */
	public static final String PARAM_USER_TYPE = "user_type";

	/**
	 * 用户名
	 */
	public static final String PARAM_ACCOUNT = "account";
	
	public String getRequestUri() {
		return this.getOrDefault(PARAM_REQUEST_URI, StringUtils.EMPTY).toString();
	}

	public void setRequestUri(String requestUri) {
		this.put(PARAM_REQUEST_URI, requestUri);
	}
	
	public String getRequestMethod() {
		return this.getOrDefault(PARAM_REQUEST_METHOD, StringUtils.EMPTY).toString();
	}

	public void setRequestMethod(String requestMethod) {
		this.put(PARAM_REQUEST_METHOD, requestMethod);
	}

	public String getRequestArgs() {
		return this.getOrDefault(PARAM_REQUEST_ARGS, StringUtils.EMPTY).toString();
	}

	public void setRequestArgs(String requestArgs) {
		this.put(PARAM_REQUEST_ARGS, requestArgs);
	}

	public Integer getResponseCode() {
		return MapUtils.getInteger(this, PARAM_RESPONSE_CODE, -1);
	}

	public void setResponseCode(Integer responseCode) {
		this.put(PARAM_RESPONSE_CODE, responseCode);
	}

	public String getResponseResult() {
		return this.getOrDefault(PARAM_RESPONSE_RESULT, StringUtils.EMPTY).toString();
	}

	public void setResponseResult(String responseResult) {
		this.put(PARAM_RESPONSE_RESULT, responseResult);
	}

	public String getIp() {
		return this.getOrDefault(PARAM_IP, StringUtils.EMPTY).toString();
	}

	public void setIp(String ip) {
		this.put(PARAM_IP, ip);
	}

	public String getUserAgent() {
		return this.getOrDefault(PARAM_USER_AGENT, StringUtils.EMPTY).toString();
	}

	public void setUserAgent(String userAgent) {
		this.put(PARAM_USER_AGENT, userAgent);
	}

	public Long getUserId() {
		return MapUtils.getLong(this, PARAM_USER_ID, 0L);
	}

	public void setUserId(Long userId) {
		this.put(PARAM_USER_ID, userId);
	}

	public String getUserType() {
		return this.getOrDefault(PARAM_USER_TYPE, StringUtils.EMPTY).toString();
	}

	public void setUserType(String userType) {
		this.put(PARAM_USER_TYPE, userType);
	}

	public String getAccount() {
		return this.getOrDefault(PARAM_ACCOUNT, StringUtils.EMPTY).toString();
	}

	public void setAccount(String account) {
		this.put(PARAM_ACCOUNT, account);
	}
	
	public String getRequestFunction() {
		return this.getOrDefault(PARAM_REQUEST_FUNCTION, StringUtils.EMPTY).toString();
	}

	public void setRequestFunction(String func) {
		this.put(PARAM_REQUEST_FUNCTION, func);
	}
}
