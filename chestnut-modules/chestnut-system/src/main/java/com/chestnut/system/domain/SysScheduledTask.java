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
package com.chestnut.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 定时任务配置表 sys_scheduled_task
 */
@Getter
@Setter
@TableName(SysScheduledTask.TABLE_NAME)
public class SysScheduledTask extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_scheduled_task";

	@TableId(value = "task_id", type = IdType.INPUT)
	private Long taskId;
	
	/**
	 * 任务类型
	 */
	private String taskType;
	
	/**
	 * 是否启用
	 */
	private String status;

	/**
	 * 触发器类型（ScheduledTaskTriggerType）
	 */
	private String taskTrigger;
	
	/**
	 * 触发器配置（JSON格式）
	 */
	private String triggerArgs;
}
