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
package com.chestnut.system.schedule;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.system.service.ISysScheduledTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
 * 异步任务构造器
 */
@Slf4j
public abstract class ScheduledTask implements Runnable {

	/**
	 * 任务ID
	 */
	private Long taskId;

	/**
	 * 任务类型
	 */
	private String type;

	/**
	 * 任务状态
	 */
	private ScheduledTaskStatus status;

	/**
	 * 任务准备执行时间，进入线程池队列的时间
	 */
	private LocalDateTime readyTime;

	/**
	 * 任务开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 任务结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 任务中断时间
	 */
	private LocalDateTime interruptTime;

	/**
	 * 任务进度百分比
	 */
	private int percent;

	/**
	 * 任务进度描述
	 */
	private String progressMessage;

	/**
	 * 任务错误信息记录
	 */
	private List<String> errMessages;

	/**
	 * 中断标识
	 */
	private boolean interrupt = false;

	private ScheduledFuture<?> future;

	private final ISysScheduledTaskService taskService;

	public ScheduledTask(ISysScheduledTaskService taskService) {
		this.taskService = taskService;
	}

	@Override
	public void run() {
		try {
			this.checkInterrupt();
			this.start();
			this.run0();
			this.completed();
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				this.setStatus(ScheduledTaskStatus.INTERRUPTED);
			} else {
				this.setStatus(ScheduledTaskStatus.FAILED);
				String err = e.getMessage();
				if (e instanceof GlobalException ge && ge.getErrorCode() != null) {
					err = I18nUtils.get(ge.getErrorCode().value(), LocaleContextHolder.getLocale(), ge.getErrArgs());
				}
				this.addErrorMessage(err);
			}
			this.setPercent(100);
			log.error("Scheduled task run failed.", e);
		} finally {
			this.onTaskEnded();
		}
		this.taskService.addTaskLog(this);
	}


	public abstract void run0() throws Exception;

	/**
	 * 提交到线程池时执行
	 */
	public void ready() {
		log.debug("[{}]ScheduledTask ready: {}[{}]", Thread.currentThread().getName(), this.getType(), this.getTaskId());
		this.status = ScheduledTaskStatus.READY;
		this.readyTime = LocalDateTime.now();
	}

	/**
	 * 任务开始执行
	 */
	void start() {
		log.debug("[{}]ScheduledTask start: {}[{}]", Thread.currentThread().getName(), this.getType(), this.getTaskId());
		this.status = ScheduledTaskStatus.RUNNING;
		this.startTime = LocalDateTime.now();
	}

	/**
	 * 任务完成
	 */
	void completed() {
		if (this.getStatus() == ScheduledTaskStatus.INTERRUPTED) {
			return;
		}
		long cost = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() - this.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
		log.debug("[{}]ScheduledTask completed: {}[{}], cost: {}ms", Thread.currentThread().getName(), this.getType(), this.getTaskId(), cost);
		this.setStatus(ScheduledTaskStatus.SUCCESS);
		this.setEndTime(LocalDateTime.now());
		this.setPercent(100);
	}

	/**
	 * 执行中断，设置中断标识
	 */
	public void interrupt() {
		log.debug("[{}]ScheduledTask interrupted: {}[{}]", Thread.currentThread().getName(), this.getType(), this.getTaskId());
		if (this.interrupt) {
			return;
		}
		this.interrupt = true;
		this.setInterruptTime(LocalDateTime.now());
	}

	/**
	 * 检查中断标识，抛出异常中断任务执行，需要run0中自行调用中断任务。
	 *
	 * @throws InterruptedException
	 */
	public void checkInterrupt() throws InterruptedException {
		Assert.isFalse(this.interrupt, () -> new InterruptedException("The scheduled task is interrupted: " + this.taskId));
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ScheduledTaskStatus getStatus() {
		return status;
	}

	public void setStatus(ScheduledTaskStatus status) {
		this.status = status;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public long getCostTime() {
		if (this.endTime != null) {
			return Duration.between(this.startTime, this.endTime).getSeconds();
		}
		return Duration.between(this.startTime, LocalDateTime.now()).getSeconds();
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public boolean isEnd() {
		return this.status == ScheduledTaskStatus.SUCCESS || this.status == ScheduledTaskStatus.FAILED
				|| this.status == ScheduledTaskStatus.INTERRUPTED;
	}

	public String getProgressMessage() {
		return progressMessage;
	}

	public void setProgressMessage(String progressInfo) {
		this.progressMessage = progressInfo;
	}

	public void setProgressInfo(int percent, String progressMessage) {
		this.percent = percent;
		this.progressMessage = progressMessage;
	}

	public void addErrorMessage(String message) {
		if (this.errMessages == null) {
			this.errMessages = new ArrayList<>();
		}
		if (this.errMessages.size() >= 100) {
			return; // 最多纪录100条错误信息
		}
		this.errMessages.add((this.errMessages.size() + 1) + "." + message);
	}

	public boolean isAlive() {
		return this.getStatus() == ScheduledTaskStatus.READY || this.getStatus() == ScheduledTaskStatus.RUNNING;
	}

	public LocalDateTime getReadyTime() {
		return readyTime;
	}

	public List<String> getErrMessages() {
		return errMessages;
	}

	public void setErrMessages(List<String> errMessages) {
		this.errMessages = errMessages;
	}

	public LocalDateTime getInterruptTime() {
		return interruptTime;
	}

	public void setInterruptTime(LocalDateTime interruptTime) {
		this.interruptTime = interruptTime;
	}

	public ScheduledFuture<?> getFuture() {
		return future;
	}

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

	public void setEndEvent(Consumer<ScheduledTask> consumer) {
		this.endEvent = consumer;
	}

	private Consumer<ScheduledTask> endEvent;

	private void onTaskEnded() {
		if (this.endEvent != null) {
			this.endEvent.accept(this);
		}
	}
}