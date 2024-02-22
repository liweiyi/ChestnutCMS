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
package com.chestnut.common.async;

import com.chestnut.common.async.enums.TaskStatus;
import com.chestnut.common.config.AsyncConfig;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器
 */
@Component
public class AsyncTaskManager {

	private final long ExpireSeconds = 60;

	private static final ThreadLocal<AsyncTask> CURRENT = new ThreadLocal<>();

	private static final ConcurrentHashMap<String, AsyncTask> asyncTaskMap = new ConcurrentHashMap<>();

	@Resource(name = AsyncConfig.COMMON_EXECUTOR_BEAN)
    private ThreadPoolTaskExecutor taskExecutor;

	AsyncTaskManager() {
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			asyncTaskMap.entrySet().removeIf(next -> next.getValue().isExpired(ExpireSeconds));
		}, ExpireSeconds, ExpireSeconds, TimeUnit.SECONDS);
	}

    /**
     * 提交异步任务，提供任务进度监控，中断操作
	 */
    public synchronized void execute(AsyncTask asyncTask) {
		if (StringUtils.isEmpty(asyncTask.getTaskId())) {
			asyncTask.setTaskId(this.generateTaskId());
		}
		AsyncTask task = asyncTaskMap.get(asyncTask.getTaskId());
		if (task != null && task.isAlive()) {
			throw CommonErrorCode.ASYNC_TASK_RUNNING.exception(asyncTask.getTaskId());
		}
		asyncTaskMap.put(asyncTask.getTaskId(), asyncTask);
        asyncTask.ready();
        this.taskExecutor.execute(asyncTask);
    }

	/**
	 * 提交异步任务
	 */
	public void execute(Runnable runnable) {
		this.taskExecutor.execute(runnable);
    }

    /**
     * 获取任务信息
     *
     * @param taskId 任务ID
     * @return AsyncTask
     */
    public AsyncTask getTask(String taskId) {
		return asyncTaskMap.get(taskId);
    }

    /**
     * 获取任务列表
	 *
     * @return taskList
     */
	public List<AsyncTask> getTaskList() {
		return new ArrayList<>(asyncTaskMap.values());
	}

    /**
     * 生成任务ID
     *
     * @return taskId
     */
    private String generateTaskId() {
        return IdUtils.randomUUID();
    }

	public synchronized void removeById(String taskId) {
		AsyncTask task = this.getTask(taskId);
		if (Objects.isNull(task)) {
			return;
		}
		boolean isRunning = task.getStatus() == TaskStatus.RUNNING || task.getStatus() == TaskStatus.READY;
		Assert.isFalse(isRunning, () -> CommonErrorCode.ASYNC_TASK_RUNNING.exception(taskId));

		asyncTaskMap.remove(taskId);
	}

	public synchronized void cancel(String taskId) {
		AsyncTask task = asyncTaskMap.get(taskId);
		if (Objects.nonNull(task)) {
			task.interrupt();
		}
	}
    
    /**
     * 获取线程池信息
     */
    public ThreadPoolTaskExecutor getThreadPool() {
		return this.taskExecutor;
    }

    protected static void setCurrent(AsyncTask task) {
		CURRENT.set(task);
    }
    
    protected static void removeCurrent() {
		CURRENT.remove();
    }
    
    public static void checkInterrupt() throws InterruptedException {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			task.checkInterrupt();
		}
    }

    public static String addErrMessage(String message) {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			task.addErrorMessage(message);
		}
		return message;
    }
    
    public static void setTaskPercent(int percent) {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			task.setPercent(percent);
		}
    }
    
    public static void setTaskMessage(String msg) {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			task.setProgressMessage(msg);
		}
    }

    public static void setTaskProgressInfo(int percent, String msg) {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			task.setProgressInfo(percent, msg);
		}
    }

	/**
	 * 设置进度条数据，进度增加剩余进度的十分之一
	 */
	public static void setTaskTenPercentProgressInfo(String msg) {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			task.setProgressInfo(task.getPercent() + (100 - task.getPercent()) / 10, msg);
		}
	}

	public static int getTaskProgressPercent() {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			return task.getPercent();
		}
		return 0;
	}
}