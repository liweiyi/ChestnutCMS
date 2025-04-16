/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.system.domain.vo.ConsoleLogsVO;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.logs.CcConsoleAppender;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制台日志记录
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorLogsView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/console")
public class SysConsoleLogController extends BaseRestController {

	@GetMapping
	public R<?> list(@RequestParam int sinceIndex) {
		CcConsoleAppender<?> instance = CcConsoleAppender.getInstance();
		Assert.notNull(instance, SysErrorCode.MISSING_CONSOLE_APPENDER::exception);

		List<CcConsoleAppender.LogEntry> logsSince = instance.getLogsSince(sinceIndex);
		List<String> logs = logsSince.stream().map(CcConsoleAppender.LogEntry::message).toList();
		ConsoleLogsVO vo = new ConsoleLogsVO(
				logsSince.isEmpty() ? sinceIndex : logsSince.get(logsSince.size() - 1).index(),
				logs
		);
		return R.ok(vo);
	}
}
