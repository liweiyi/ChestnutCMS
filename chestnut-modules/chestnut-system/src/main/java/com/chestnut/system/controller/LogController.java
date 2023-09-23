package com.chestnut.system.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.system.logs.ILogMenu;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 日志基础控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/logs")
public class LogController extends BaseRestController {

	private final List<ILogMenu> logMenus;

	@Priv(type = AdminUserType.TYPE)
	@GetMapping
	public R<?> getMenus() throws Exception {
		return this.bindDataTable(this.logMenus);
	}
}
