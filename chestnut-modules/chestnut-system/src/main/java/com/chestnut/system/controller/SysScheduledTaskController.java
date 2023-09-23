package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysScheduledTask;
import com.chestnut.system.domain.SysScheduledTaskLog;
import com.chestnut.system.domain.dto.ScheduledTaskDTO;
import com.chestnut.system.domain.vo.ScheduledTaskVO;
import com.chestnut.system.mapper.SysScheduledTaskLogMapper;
import com.chestnut.system.schedule.IScheduledHandler;
import com.chestnut.system.schedule.ScheduledTask;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysScheduledTaskService;
import com.chestnut.system.validator.LongId;
import com.xxl.job.core.handler.IJobHandler;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/task")
public class SysScheduledTaskController extends BaseRestController {
	
	private final ISysScheduledTaskService taskService;

	private final SysScheduledTaskLogMapper logMapper;
	
	private final Map<String, IScheduledHandler> taskHandlers;
	
	@GetMapping("/typeOptions")
	public R<?> getTaskTypeOptions() {
		List<Map<String, String>> list = this.taskHandlers.values().stream()
				.sorted(Comparator.comparing(IScheduledHandler::getId))
				.map(t -> Map.of("label", I18nUtils.get(t.getName()), "value", t.getId()))
				.toList();
		return R.ok(list);
	}
	
	@GetMapping
	public R<?> list(@RequestParam(required = false) String status) {
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

	@GetMapping("/{taskId}")
	public R<?> getInfo(@PathVariable Long taskId) {
		SysScheduledTask task = this.taskService.getById(taskId);
		Assert.notNull(task, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("taskId", taskId));
		
		
		return R.ok(task);
	}

	@PostMapping
	public R<?> add(@Validated @RequestBody ScheduledTaskDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		taskService.insertTask(dto);
		return R.ok();
	}

	@PutMapping
	public R<?> edit(@Validated @RequestBody ScheduledTaskDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		taskService.updateTask(dto);
		return R.ok();
	}

	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> taskIds) {
		Assert.isTrue(IdUtils.validate(taskIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		taskService.deleteTasks(taskIds);
		return R.ok();
	}

	@PutMapping("/enable/{taskId}")
	public R<?> enable(@PathVariable @LongId Long taskId) {
		taskService.enableTask(taskId);
		return R.ok();
	}

	@PutMapping("/disable/{taskId}")
	public R<?> disable(@PathVariable @LongId Long taskId) {
		taskService.disableTask(taskId);
		return R.ok();
	}

	@PostMapping("/exec/{taskId}")
	public R<?> execTaskOnce(@PathVariable @LongId Long taskId) {
		taskService.execOnceImmediately(taskId);
		return R.ok();
	}

	@GetMapping("/logs")
	public R<?> getTaskLogs(@RequestParam @LongId Long taskId) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysScheduledTaskLog> q = new LambdaQueryWrapper<SysScheduledTaskLog>()
				.eq(SysScheduledTaskLog::getTaskId, taskId)
				.orderByDesc(SysScheduledTaskLog::getLogId);
		Page<SysScheduledTaskLog> page = this.logMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return this.bindDataTable(page);
	}

	@DeleteMapping("/logs")
	public R<?> removeLogs(@RequestBody @NotEmpty List<Long> logIds) {
		Assert.isTrue(IdUtils.validate(logIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		this.logMapper.deleteBatchIds(logIds);
		return R.ok();
	}
}
