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
package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysOperLog;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysOperLogService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 操作日志记录
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorLogsView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseRestController {

	private final ISysOperLogService operLogService;

	@ExcelExportable(SysOperLog.class)
	@GetMapping("/list")
	public R<?> list(SysOperLog operLog) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysOperLog> q = new LambdaQueryWrapper<SysOperLog>()
				.like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
				.like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
				.eq(Objects.nonNull(operLog.getOperatorType()), SysOperLog::getOperatorType, operLog.getOperatorType())
				.eq(Objects.nonNull(operLog.getResponseCode()), SysOperLog::getResponseCode, operLog.getResponseCode())
				.ge(Objects.nonNull(operLog.getParams().get("beginTime")), SysOperLog::getOperTime, operLog.getParams().get("beginTime"))
				.le(Objects.nonNull(operLog.getParams().get("endTime")), SysOperLog::getOperTime, operLog.getParams().get("endTime"))
				.orderByDesc(SysOperLog::getOperId);
		Page<SysOperLog> page = operLogService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@Log(title = "操作日志", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> operIds) {
		operLogService.removeByIds(operIds);
		return R.ok();
	}

	@Log(title = "操作日志", businessType = BusinessType.CLEAN)
	@DeleteMapping("/clean")
	public R<?> clean() {
		operLogService.cleanOperLog();
		return R.ok();
	}
}
