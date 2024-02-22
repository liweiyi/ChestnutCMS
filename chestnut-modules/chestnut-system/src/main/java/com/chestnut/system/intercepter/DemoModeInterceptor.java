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
package com.chestnut.system.intercepter;

import java.util.Objects;

import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.exception.SecurityErrorCode;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.security.StpAdminUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 演示模式拦截器
 */
@RequiredArgsConstructor
public class DemoModeInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String method = request.getMethod();
		if (StpAdminUtil.isLogin() && SecurityUtils.isSuperAdmin(StpAdminUtil.getLoginIdAsLong())) {
			return true; // 超级管理员允许操作
		}
		// 非Get且无忽略演示模式注解则抛出异常
		if (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) {
			if (handler instanceof HandlerMethod handlerMethod) {
				IgnoreDemoMode ignoreDemoMode = handlerMethod.getMethodAnnotation(IgnoreDemoMode.class);
				if (Objects.isNull(ignoreDemoMode)) {
					throw SecurityErrorCode.DEMO_EXCEPTION.exception();
				}
			}
		}
		return true;
	}
}
