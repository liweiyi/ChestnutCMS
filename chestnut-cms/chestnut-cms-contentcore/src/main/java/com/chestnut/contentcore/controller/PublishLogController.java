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
package com.chestnut.contentcore.controller;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;


import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.log.PublishLogAppender;
import com.chestnut.system.domain.vo.ConsoleLogsVO;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.logs.CcConsoleAppender;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发布日志管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.PUBLISH_LOG.MODULE}")
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/publish/")
public class PublishLogController extends BaseRestController {

	private final IPublishStrategy publishStrategy;

	/**
	 * 发布队列任务数量
	 */
	@XComment("{API.DOC.CMS.PUBLISH_LOG.GET_TASK_COUNT}")
	@GetMapping("/taskCount")
	public R<Long> getPublishTaskCount() {
		return R.ok(publishStrategy.getTaskCount());
	}

	/**
	 * 清理发布队列
	 */
	@XComment("{API.DOC.CMS.PUBLISH_LOG.CLEAR_TASKS}")
	@PostMapping("/clear")
	public R<Void> clearPublishTask() {
		publishStrategy.cleanTasks();
		return R.ok();
	}

	@XComment("{API.DOC.CMS.PUBLISH_LOG.GET_LIST}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorLogsView)
	@GetMapping("/logs")
	public R<ConsoleLogsVO> list(@RequestParam @XComment("{API.DOC.CMS.PUBLISH_LOG.SINCE_INDEX}") int sinceIndex) {
		PublishLogAppender<?> instance = PublishLogAppender.getInstance();
		Assert.notNull(instance, SysErrorCode.MISSING_CONSOLE_APPENDER::exception);

		List<PublishLogAppender.LogEntry> logsSince = instance.getLogsSince(sinceIndex);
		List<String> logs = logsSince.stream().map(PublishLogAppender.LogEntry::message).toList();
		ConsoleLogsVO vo = new ConsoleLogsVO(
				logsSince.isEmpty() ? sinceIndex : logsSince.get(logsSince.size() - 1).index(),
				logs
		);
		return R.ok(vo);
	}
}
