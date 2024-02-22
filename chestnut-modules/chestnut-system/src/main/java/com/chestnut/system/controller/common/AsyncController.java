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
package com.chestnut.system.controller.common;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.vo.AsyncTaskVO;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/async")
public class AsyncController extends BaseRestController {

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 异步任务列表
	 * 
	 * @param type 任务类型
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@GetMapping("/task")
	public R<?> getAsyncTaskList(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "id", required = false) String taskId) {
		List<AsyncTask> taskList = this.asyncTaskManager.getTaskList();
		List<AsyncTaskVO> list = taskList.stream()
				.filter(t -> (StringUtils.isBlank(type) || t.getType().contains(type))
					&& (StringUtils.isBlank(taskId) || t.getTaskId().contains(taskId)))
				.map(AsyncTaskVO::new)
				.sorted(Comparator.comparing(AsyncTaskVO::getReadyTime).reversed())
				.toList();
		return this.bindDataTable(list);
	}

	/**
	 * 异步任务详情
	 * 
	 * @param taskId 任务ID
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/task/{taskId}")
	public R<?> getAsyncTaskInfo(@PathVariable("taskId") String taskId) {
		AsyncTask task = this.asyncTaskManager.getTask(taskId);
		Assert.notNull(task, () -> SysErrorCode.ASYNC_TASK_NOT_FOUND.exception(taskId));

		return R.ok(new AsyncTaskVO(task));
	}

	/**
	 * 停止异步任务
	 * 
	 * @param taskIds
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PutMapping("/task/stop")
	public R<?> stopAsyncTask(@RequestBody @NotEmpty List<String> taskIds) {
		for (String taskId : taskIds) {
			this.asyncTaskManager.cancel(taskId);
		}
		return R.ok();
	}

	/**
	 * 删除异步任务
	 * 
	 * @param taskIds
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@DeleteMapping("task/remove")
	public R<?> deleteAsyncTask(@RequestBody @NotEmpty List<String> taskIds) {
		for (String taskId : taskIds) {
			this.asyncTaskManager.removeById(taskId);
		}
		return R.ok();
	}
}
