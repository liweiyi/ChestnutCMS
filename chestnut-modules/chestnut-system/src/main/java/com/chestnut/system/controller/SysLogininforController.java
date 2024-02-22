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
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysLogininfor;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysLogininforService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 系统访问记录
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorLogsView)
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseRestController {

	private final ISysLogininforService logininforService;

	@ExcelExportable(SysLogininfor.class)
	@GetMapping("/list")
	public R<TableData<SysLogininfor>> list(SysLogininfor logininfor, HttpServletRequest request) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysLogininfor> q = new LambdaQueryWrapper<SysLogininfor>()
				.like(StringUtils.isNotEmpty(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName())
				.like(StringUtils.isNotEmpty(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
				.eq(StringUtils.isNotEmpty(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
				.ge(Objects.nonNull(logininfor.getParams().get("beginTime")), SysLogininfor::getLoginTime, logininfor.getParams().get("beginTime"))
				.le(Objects.nonNull(logininfor.getParams().get("endTime")), SysLogininfor::getLoginTime, logininfor.getParams().get("endTime"))
				.orderByDesc(SysLogininfor::getInfoId);
		Page<SysLogininfor> page = logininforService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		LoginLogType.decode(page.getRecords(), SysLogininfor::getLogType, SysLogininfor::setLogType);
		return bindDataTable(page);
	}

	@Log(title = "登录日志", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> infoIds) {
		logininforService.removeByIds(infoIds);
		return R.ok();
	}

	@Log(title = "登录日志", businessType = BusinessType.CLEAN)
	@DeleteMapping("/clean")
	public R<?> clean() {
		logininforService.cleanLogininfor();
		return R.ok();
	}
}
