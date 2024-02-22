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

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.system.domain.SysScheduledTask;
import com.chestnut.system.schedule.ScheduledTask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledTaskVO {
	
	/**
	 * 任务ID
	 */
	private Long taskId;
	
	/**
	 * 任务类型
	 */
	private String taskType;

	private String taskTypeName;
	
	/**
	 * 任务状态
	 */
	private String status;
	
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
	
	public ScheduledTaskVO(SysScheduledTask task, ScheduledTask scheduledTask) {
		this.taskId = task.getTaskId();
		this.taskType = task.getTaskType();
		this.status = task.getStatus();
		if (scheduledTask != null) {
			this.readyTime = scheduledTask.getReadyTime();
			this.startTime = scheduledTask.getStartTime();
			this.endTime = scheduledTask.getEndTime();
		}
	}
}

