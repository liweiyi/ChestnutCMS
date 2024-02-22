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
package com.chestnut.common.log;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;

import com.chestnut.common.log.annotation.Log;

/**
 * 日志类型接口，收集日志相关信息交给LogHandler处理。
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ILogType {
	
	public static final String BEAN_NAME_PREFIX = "LogType_";

	/**
	 * 日志类型
	 */
	public String getType();

	/**
	 * 注解目标执行前日志处理
	 * 
	 * @param joinPoint
	 * @param log
	 * @param logTime
	 */
	default public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
		
	}
	
	/**
	 * 注解目标执行后日志处理
	 * 
	 * @param joinPoint
	 * @param log
	 * @param logTime
	 * @param result
	 * @param e
	 */
	default public void afterProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime, Object result, Throwable e) {
		
	}
}
