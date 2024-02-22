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
package com.chestnut.common.extend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.chestnut.common.extend.ExtendConstants;
import com.chestnut.common.extend.enums.LimitType;

/**
 * 限流注解
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
	
	/**
	 * 限流缓存key前缀
	 */
	public String prefix() default ExtendConstants.RATE_LIMIT_KEY;

	/**
	 * 限流时间,单位秒
	 */
	public int expire() default 60;

	/**
	 * 限流阈值，单位时间内的请求上限
	 */
	public int limit() default 100;

	/**
	 * 限流类型
	 */
	public LimitType limitType() default LimitType.DEFAULT;
}
