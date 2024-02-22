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
package com.chestnut.common.extend.aspectj;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.extend.annotation.RateLimiter;
import com.chestnut.common.extend.enums.LimitType;
import com.chestnut.common.extend.exception.RateLimiterErrorCode;
import com.chestnut.common.utils.ServletUtils;

import lombok.RequiredArgsConstructor;

/**
 * 限流处理
 */
@Aspect
@RequiredArgsConstructor
public class RateLimiterAspect {
	
	private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

	private final RedisTemplate<String, Object> redisTemplate;

	private final RedisScript<Boolean> limitScript;

	@Before("@annotation(rateLimiter)")
	public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable {
		int limit = rateLimiter.limit();
		int expire = rateLimiter.expire();

		try {
			String combineKey = this.getCombineKey(rateLimiter, point);
			List<String> keys = List.of(combineKey);
			if (!redisTemplate.execute(this.limitScript, keys, limit, expire)) {
				log.warn("限制请求'{}',缓存key'{}'", limit, combineKey);
				throw RateLimiterErrorCode.RATE_LIMIT.exception();
			}
		} catch (GlobalException e) {
			throw e;
		} catch (Exception e) {
			throw RateLimiterErrorCode.RATE_LIMIT_ERR.exception();
		}
	}

	public String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
		StringBuffer stringBuffer = new StringBuffer(rateLimiter.prefix());
		if (rateLimiter.limitType() == LimitType.IP) {
			stringBuffer.append(ServletUtils.getIpAddr(ServletUtils.getRequest())).append(".");
		}
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Class<?> targetClass = method.getDeclaringClass();
		stringBuffer.append(targetClass.getName()).append(".").append(method.getName());
		return stringBuffer.toString();
	}
}
