package com.chestnut.system.controller;

import com.chestnut.common.config.properties.ChestnutProperties;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.system.domain.vo.server.Server;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author 兮玥（190785909@qq.com）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/server")
public class ServerController {

	private final ChestnutProperties properties;
	
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorServerList)
	@GetMapping()
	public R<?> getInfo() throws Exception {
		Server server = new Server();
		server.copyTo();
		server.getApp().setName(properties.getName());
		server.getApp().setVersion(properties.getVersion());
		return R.ok(server);
	}
}
