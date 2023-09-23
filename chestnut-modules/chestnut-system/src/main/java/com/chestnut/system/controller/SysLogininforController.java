package com.chestnut.system.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysLogininfor;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysLogininforService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

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
	
	@GetMapping("/list")
	public R<?> list(SysLogininfor logininfor) {
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

	@Log(title = "登录日志", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysLogininfor logininfor) {
		LambdaQueryWrapper<SysLogininfor> q = new LambdaQueryWrapper<SysLogininfor>()
				.like(StringUtils.isNotEmpty(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName())
				.like(StringUtils.isNotEmpty(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
				.eq(StringUtils.isNotEmpty(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
				.ge(Objects.nonNull(logininfor.getParams().get("beginTime")), SysLogininfor::getLoginTime, logininfor.getParams().get("beginTime"))
				.le(Objects.nonNull(logininfor.getParams().get("endTime")), SysLogininfor::getLoginTime, logininfor.getParams().get("endTime"))
				.orderByDesc(SysLogininfor::getInfoId);
		List<SysLogininfor> list = logininforService.list(q);
		LoginLogType.decode(list, SysLogininfor::getLogType, SysLogininfor::setLogType);
		this.exportExcel(list, SysLogininfor.class, response);
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
