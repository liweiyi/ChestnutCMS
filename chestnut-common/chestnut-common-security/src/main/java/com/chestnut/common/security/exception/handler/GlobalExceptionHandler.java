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
package com.chestnut.common.security.exception.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.exception.SecurityErrorCode;
import com.chestnut.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * 自定义通用异常
	 */
	@ExceptionHandler(GlobalException.class)
	public R<?> handleGlobalException(GlobalException e) {
		if (StringUtils.isNotEmpty(e.getMessage())) {
			return R.fail(e.getMessage());
		}
		log.error("Global error: {}", e.getMessage(), e);
		return R.fail(I18nUtils.get(e.getErrorCode().value(), LocaleContextHolder.getLocale(), e.getErrArgs()));
	}

	/**
	 * 未登录
	 */
	@ExceptionHandler(NotLoginException.class)
	public R<?> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
		log.error("NotLogin[code: {}, type: {}]{}", e.getCode(), e.getType(), e.getMessage());
		return R.fail(HttpStatus.UNAUTHORIZED.value(), I18nUtils.get(SecurityErrorCode.NOT_LOGIN.value()));
	}

	/**
	 * 权限校验异常
	 */
	@ExceptionHandler(NotPermissionException.class)
	public R<?> handleSecurityPermissionException(NotPermissionException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
		return R.fail(HttpStatus.FORBIDDEN.value(), I18nUtils.get(SecurityErrorCode.NOT_PERMISSION.value(), e.getPermission()));
	}

	/**
	 * 请求方式不支持
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public R<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
			HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
		return R.fail(e.getMessage());
	}

	/**
	 * 拦截未知的运行时异常
	 */
	@ExceptionHandler(RuntimeException.class)
	public R<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',发生未知异常.", requestURI, e);
		return R.fail(e.getMessage());
	}

	/**
	 * 系统异常
	 */
	@ExceptionHandler(Exception.class)
	public R<?> handleException(Exception e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.error("请求地址'{}',发生系统异常.", requestURI, e);
		return R.fail(e.getMessage());
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(BindException.class)
	public R<?> handleBindException(BindException e) {
		String errMsg = e.getBindingResult().getAllErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining("\n"));
		return R.fail(errMsg);
	}
}
