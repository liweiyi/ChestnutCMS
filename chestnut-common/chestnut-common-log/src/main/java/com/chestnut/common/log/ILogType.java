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
