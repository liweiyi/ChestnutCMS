/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;


import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysScheduledTask;
import com.chestnut.system.domain.SysScheduledTaskLog;
import com.chestnut.system.domain.dto.CreateScheduledTaskRequest;
import com.chestnut.system.domain.dto.UpdateScheduledTaskRequest;
import com.chestnut.system.domain.vo.ScheduledTaskVO;
import com.chestnut.system.mapper.SysScheduledTaskLogMapper;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.schedule.IScheduledHandler;
import com.chestnut.system.schedule.ScheduledTask;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysScheduledTaskService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 定时任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.TASK.MODULE}")
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/task")
public class SysScheduledTaskController extends BaseRestController {
	
	private final ISysScheduledTaskService taskService;

	private final SysScheduledTaskLogMapper logMapper;
	
	private final Map<String, IScheduledHandler> taskHandlers;
	@XComment("{API.DOC.SYS.TASK.GET_TYPE_OPTIONS}")
	@GetMapping("/typeOptions")
	public R<?> getTaskTypeOptions() {
		List<IScheduledHandler> list = this.taskHandlers.values().stream()
				.sorted(Comparator.comparing(IScheduledHandler::getId))
				.toList();
		return bindSelectOptions(list, IScheduledHandler::getId, IScheduledHandler::getName);
	}

	@XComment("{API.DOC.SYS.TASK.GET_LIST}")
	@GetMapping("/list")
	public R<TableData<ScheduledTaskVO>> list(@RequestParam(required = false) @XComment("{API.DOC.SYS.TASK.STATUS}") String status) {
		PageRequest pr = this.getPageRequest();
		Page<SysScheduledTask> page = this.taskService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(status), SysScheduledTask::getStatus, status)
				.orderByDesc(SysScheduledTask::getTaskId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		List<ScheduledTaskVO> list = page.getRecords().stream().map(task -> {
			ScheduledTask scheduledTask = this.taskService.getScheduledTask(task.getTaskId());
			ScheduledTaskVO vo = new ScheduledTaskVO(task, scheduledTask);
			IScheduledHandler handler = taskHandlers.get(IScheduledHandler.BEAN_PREFIX + task.getTaskType());
			if (handler != null) {
				vo.setTaskTypeName(I18nUtils.get(handler.getName()));
			}
			return vo;
		}).toList();
		return bindDataTable(list, page.getTotal());
	}
	@XComment("{API.DOC.SYS.TASK.GET_INFO}")
	@GetMapping("/detail/{taskId}")
	public R<SysScheduledTask> getInfo(@PathVariable @LongId @XComment("{API.DOC.SYS.TASK.ID}") Long taskId) {
		SysScheduledTask task = this.taskService.getById(taskId);
		Assert.notNull(task, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("taskId", taskId));
		task.setData(JacksonUtils.parse(task.getTriggerArgs()));
		return R.ok(task);
	}

	@XComment("{API.DOC.SYS.TASK.CREATE_TASK}")
	@Log(title = "定时任务", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PostMapping("/add")
	public R<Void> add(@Validated @RequestBody CreateScheduledTaskRequest dto) {
		taskService.insertTask(dto);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.TASK.UPDATE_TASK}")
	@Log(title = "定时任务", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PostMapping("/update")
	public R<Void> edit(@Validated @RequestBody UpdateScheduledTaskRequest dto) {
		taskService.updateTask(dto);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.TASK.DELETE_TASK}")
	@Log(title = "定时任务", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.TASK.IDS}") List<Long> taskIds) {
		taskService.deleteTasks(taskIds);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.TASK.ENABLE}")
	@Log(title = "定时任务", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PostMapping("/enable/{taskId}")
	public R<Void> enable(@PathVariable @LongId @XComment("{API.DOC.SYS.TASK.ID}") Long taskId) {
		taskService.enableTask(taskId);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.TASK.DISABLE}")
	@Log(title = "定时任务", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PostMapping("/disable/{taskId}")
	public R<Void> disable(@PathVariable @LongId @XComment("{API.DOC.SYS.TASK.ID}") Long taskId) {
		taskService.disableTask(taskId);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.TASK.EXEC}")
	@Log(title = "定时任务", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PostMapping("/exec/{taskId}")
	public R<Void> execTaskOnce(@PathVariable @LongId @XComment("{API.DOC.SYS.TASK.ID}") Long taskId) {
		taskService.execOnceImmediately(taskId);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.TASK.GET_LOGS}")
	@GetMapping("/logs")
	public R<TableData<SysScheduledTaskLog>> getTaskLogs(@RequestParam @LongId @XComment("{API.DOC.SYS.TASK.ID}") Long taskId) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysScheduledTaskLog> q = new LambdaQueryWrapper<SysScheduledTaskLog>()
				.eq(SysScheduledTaskLog::getTaskId, taskId)
				.orderByDesc(SysScheduledTaskLog::getLogId);
		Page<SysScheduledTaskLog> page = this.logMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.SYS.TASK.DELETE_LOGS}")
	@Log(title = "定时任务日志", businessType = BusinessType.DELETE)
	@PostMapping("/logs/delete")
	public R<Void> removeLogs(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.TASK.LOG_IDS}") List<Long> logIds) {
		this.logMapper.deleteByIds(logIds);
		return R.ok();
	}
}