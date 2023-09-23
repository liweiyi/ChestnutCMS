package com.chestnut.common.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.chestnut.common.async.enums.TaskStatus;
import com.chestnut.common.config.AsyncConfig;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

/**
 * 异步任务管理器
 */
@RequiredArgsConstructor
@Component
public class AsyncTaskManager {
	
	private static final ThreadLocal<AsyncTask> CURRENT = new ThreadLocal<>();

	private ConcurrentHashMap<String, AsyncTask> asyncTaskMap = new ConcurrentHashMap<>();

	@Resource(name = AsyncConfig.COMMON_EXECUTOR_BEAN)
    private ThreadPoolTaskExecutor taskExecutor;


	public void execute(AsyncTask asyncTask) {
		execute(asyncTask, false);
	}

	public void executeIgnoreIfExists(AsyncTask asyncTask) {
		this.execute(asyncTask, true);
	}

    /**
     * 提交异步任务
     * 
     * @param asyncTask 异步任务构造器
     * @return taskInfo
     */
    public synchronized void execute(AsyncTask asyncTask, boolean ignoreExists) {
    	if (StringUtils.isEmpty(asyncTask.getTaskId())) {
    		asyncTask.setTaskId(this.generateTaskId());
    	}
    	AsyncTask task = this.asyncTaskMap.get(asyncTask.getTaskId());
    	if (task != null && task.isAlive()) {
			if (ignoreExists) {
				return;
			}
    		throw CommonErrorCode.ASYNC_TASK_RUNNING.exception(asyncTask.getTaskId());
    	}
        this.asyncTaskMap.put(asyncTask.getTaskId(), asyncTask);
        asyncTask.ready();

        this.taskExecutor.execute(asyncTask);
    }
    
    public void execute(Runnable runnable) {
		this.execute(new AsyncTask() {
			
			@Override
			public void run0() throws Exception {
				runnable.run();
			}
		});
    }

    /**
     * 获取任务信息
     *
     * @param taskId 任务ID
     * @return
     */
    public AsyncTask getTask(String taskId) {
    	return this.asyncTaskMap.get(taskId);
    }

    /**
     * 获取任务列表
     * @return
     */
	public List<AsyncTask> getTaskList() {
		return new ArrayList<>(this.asyncTaskMap.values());
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

		this.asyncTaskMap.remove(taskId);
	}
	
	public synchronized void cancel(String taskId) {
		AsyncTask task = this.asyncTaskMap.get(taskId);
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

	public static int getTaskProgressPercent() {
		AsyncTask task = CURRENT.get();
		if (Objects.nonNull(task)) {
			return task.getPercent();
		}
		return 0;
	}
}