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

import com.chestnut.common.domain.R;
import com.chestnut.common.log.ILogHandler;
import com.chestnut.common.log.ILogType;
import com.chestnut.common.log.LogDetail;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.aspectj.LogAspect;
import com.chestnut.common.security.IUserType;
import com.chestnut.common.security.SecurityService;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Restful Controller 访问日志
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(RestfulLogType.TYPE)
public class RestfulLogType implements ILogType {
	
    protected Logger logger = LoggerFactory.getLogger(RestfulLogType.class);

	public final static String TYPE = BEAN_NAME_PREFIX + "Restful";

	/**
	 * 登录用户临时变量名
	 */
	public final static String LOG_ASPECT_LOGIN_USER = "LOGIN_USER";
	
	/**
	 * 日志处理器
	 */
	private final List<ILogHandler> logHandlers;
	
	private final SecurityService securityService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
		try {
			if (joinPoint.getSignature() instanceof MethodSignature ms) {
				Priv priv = ms.getMethod().getAnnotation(Priv.class);
				if (Objects.isNull(priv)) {
					priv = ms.getMethod().getDeclaringClass().getAnnotation(Priv.class);
				}
				if (Objects.nonNull(priv)) {
					String userType = priv.type();
					IUserType ut = this.securityService.getUserType(userType);
					LoginUser loginUser = ut.getLoginUser();
					LogAspect.put(LOG_ASPECT_LOGIN_USER, loginUser);
				}
			}
		} catch (Exception e) {
			logger.error("[RestfulLogType.beforeProceed]Try to get login user failed.", e);
		}
	}

	@Override
	public void afterProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime, Object result, Throwable e) {
		try {
			LogDetail logDetail = new LogDetail();
			// 注解信息
			logDetail.setLogTitle(log.title());
			logDetail.setBusinessType(log.businessType().name());
			logDetail.setLogType(log.type());

			logDetail.setLogTime(logTime);
			logDetail.setCost(Duration.between(logTime, LocalDateTime.now()).toMillis());

			// restfull log data
			RestfulLogData logData = new RestfulLogData();
			// 操作用户信息
			Optional<?> opt = LogAspect.get(LOG_ASPECT_LOGIN_USER);
			opt.ifPresent(o -> {
				LoginUser loginUser = (LoginUser) o;
				logData.setUserId(loginUser.getUserId());
				logData.setUserType(loginUser.getUserType());
				logData.setAccount(loginUser.getUsername());
			});
			// 请求信息
			this.fillRequestData(joinPoint, log, logData);
			// 返回信息
			if (Objects.nonNull(result)) {
				if (result instanceof R<?> r) {
					logData.setResponseCode(r.getCode());
				} else {
					logData.setResponseCode(HttpStatus.OK.value());
				}
				if (log.isSaveResponseData()) {
					logData.setResponseResult(JacksonUtils.to(result));
				}
			} else if (Objects.nonNull(e)) {
				// 异常信息
				logData.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				logData.setResponseResult(e.getMessage());
			} else {
				logData.setResponseCode(ServletUtils.getResponse().getStatus());
			}
			logDetail.setDetails(logData);
			// 处理日志
			this.logHandlers.forEach(h -> {
				if (h.test(RestfulLogType.TYPE)) {
					h.handler(logDetail);
				}
			});
		} catch (Exception ex) {
			logger.error("[RestfulLogType.afterProceed]Handle log error.", e);
		}
	}

	private void fillRequestData(ProceedingJoinPoint joinPoint, Log log, RestfulLogData logData) {
		HttpServletRequest request = ServletUtils.getRequest();
		logData.setRequestUri(request.getRequestURI());
		logData.setRequestMethod(request.getMethod());
		logData.setRequestFunction(joinPoint.getSignature().toString());

		logData.setUserAgent(ServletUtils.getHeader(request, ServletUtils.HEADER_USER_AGENT));
		logData.setIp(ServletUtils.getIpAddr(request));

		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		String[] argNames = methodSignature.getParameterNames();

		if (log.isSaveRequestData()) {
			Object[] argValues = joinPoint.getArgs();
			HashMap<String, Object> args = new HashMap<>(argValues.length);
			for (int i = 0; i < argNames.length; i++) {
				String argName = argNames[i];
				Object argValue = argValues[i];
				if (argValue == null) {
					args.put(argName, argValue);
				} else if (!this.isIgnoreArgs(argValue)) {
					args.put(argName, argValue);
				}
			}
			String requestArgs = JacksonUtils.to(args);
			logData.setRequestArgs(requestArgs);
		}
	}

	/**
	 * 判断是否需要过滤的对象。
	 * 
	 * @param o
	 *            对象信息。
	 * @return 如果是需要过滤的对象，则返回true；否则返回false。
	 */
	private boolean isIgnoreArgs(final Object o) {
		Class<?> clazz = o.getClass();
		if (clazz.isArray()) {
			return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
		} else if (Collection.class.isAssignableFrom(clazz)) {
			Collection<?> collection = (Collection<?>) o;
			return collection.stream().anyMatch(this::isIgnoreArgs);
		} else if (Map.class.isAssignableFrom(clazz)) {
			return ((Map<?, ?>) o).values().stream().anyMatch(this::isIgnoreArgs);
		}
		return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
				|| o instanceof BindingResult;
	}
}
