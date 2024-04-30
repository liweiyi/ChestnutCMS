package com.chestnut.common.async;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import com.chestnut.common.async.enums.TaskStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * 异步任务异常处理器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
public class AsyncTaskExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		if (ArrayUtils.isNotEmpty(params) && params[0] instanceof AsyncTask task) {
			task.setStatus(TaskStatus.FAILED);
			task.setEndTime(LocalDateTime.now());
			task.addErrorMessage(ex.getMessage());
			AsyncTaskManager.setTaskMessage("Async task failed：" + ex.getMessage());
			log.error("Async task failed：{}", ex.getMessage());
		}
	}
}