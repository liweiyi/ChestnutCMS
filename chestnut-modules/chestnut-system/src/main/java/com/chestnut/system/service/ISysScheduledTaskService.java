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
package com.chestnut.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysScheduledTask;
import com.chestnut.system.domain.dto.ScheduledTaskDTO;
import com.chestnut.system.schedule.ScheduledTask;

/**
 * 定时任务 服务层
 * 
 * 未开启XXL-JOB时默认使用的一个简单本地定时任务，不支持集群
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysScheduledTaskService extends IService<SysScheduledTask> {

	/**
	 * 新增定时任务
	 * 
	 * @param task
	 */
	void insertTask(ScheduledTaskDTO task);

	/**
	 * 编辑定时任务信息
	 * 
	 * @param task
	 */
	void updateTask(ScheduledTaskDTO task);

	/**
	 * 删除定时任务
	 * 
	 * @param taskIds
	 */
	void deleteTasks(List<Long> taskIds);

	/**
	 * 立即执行一次
	 */
	void execOnceImmediately(Long taskId);

	/**
	 * 添加任务执行日志
	 * 
	 * @param scheduledTask
	 */
	void addTaskLog(ScheduledTask scheduledTask);

	/**
	 * 启用定时任务
	 * 
	 * @param taskId
	 */
	void enableTask(Long taskId);

	/**
	 * 禁用定时任务
	 * 
	 * @param taskId
	 */
	void disableTask(Long taskId);
	
	/**
	 * 获取启用的定时任务实例
	 * 
	 * @param taskId
	 * @return
	 */
	ScheduledTask getScheduledTask(Long taskId);
}
