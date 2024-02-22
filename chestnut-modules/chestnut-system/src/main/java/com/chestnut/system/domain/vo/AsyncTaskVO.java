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
package com.chestnut.system.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.enums.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsyncTaskVO {
	
	/**
	 * 任务ID
	 */
	private String taskId;
	
	/**
	 * 任务类型
	 */
	private String type;
	
	/**
	 * 任务状态
	 */
	private TaskStatus status;
	
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
	
	public AsyncTaskVO(AsyncTask task) {
		this.taskId = task.getTaskId();
		this.type = task.getType();
		this.status = task.getStatus();
		this.readyTime = task.getReadyTime();
		this.startTime = task.getStartTime();
		this.endTime = task.getEndTime();
		this.percent = task.getPercent();
		this.progressMessage = task.getProgressMessage();
		this.errMessages = task.getErrMessages();
	}
}

