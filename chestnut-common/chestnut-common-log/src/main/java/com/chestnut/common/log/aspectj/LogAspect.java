package com.chestnut.common.log.aspectj;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.chestnut.common.log.ILogType;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * 日志处理切面实现
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

	/**
	 * 日志记录临时变量上下文
	 */
	private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

	private final Map<String, ILogType> logTypes;
	
	@Around("@annotation(log)")
	public Object around(ProceedingJoinPoint joinPoint, Log log) throws Throwable {
		ILogType logType = logTypes.get(log.type());
		if (logType == null) {
			return joinPoint.proceed();
		}
		LocalDateTime logTime = LocalDateTime.now();
		try {
			logType.beforeProceed(joinPoint, log, logTime);
			Object result = joinPoint.proceed();
			logType.afterProceed(joinPoint, log, logTime, result, null);
			return result;
		} catch (Throwable exception) {
			logType.afterProceed(joinPoint, log, logTime, null, exception);
			throw exception;
		} finally {
			clearThreadLocal();
		}
	}
	
	public static void put(String key, Object value) {
		if (Objects.isNull(CONTEXT.get())) {
			CONTEXT.set(new HashMap<>());
		}
		CONTEXT.get().put(key, value);
	}
	
	public static Optional<?> get(String key) {
		if (Objects.isNull(CONTEXT.get())) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(CONTEXT.get().get(key));
	}
	
	public static String getString(String key) {
		if (Objects.isNull(CONTEXT.get())) {
			return StringUtils.EMPTY;
		}
		Object v = CONTEXT.get().get(key);
		return Objects.isNull(v) ? StringUtils.EMPTY : v.toString();
	}

	public static long getLongValue(String key) {
		if (Objects.isNull(CONTEXT.get())) {
			return 0L;
		}
		Object v = CONTEXT.get().get(key);
		return Objects.isNull(v) ? 0L : NumberUtils.toLong(v.toString());
	}
	
	private static void clearThreadLocal() {
		CONTEXT.remove();
	}
}
