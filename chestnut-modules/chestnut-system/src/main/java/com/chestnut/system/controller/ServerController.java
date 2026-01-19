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

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.chestnut.common.config.properties.ChestnutProperties;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.system.domain.vo.server.AppInfo;
import com.chestnut.system.domain.vo.server.Server;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

	private final DynamicDataSourceProperties dataSourceProperties;
	
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorServerList)
	@GetMapping()
	public R<?> getInfo() throws Exception {
		Server server = new Server();
		server.copyTo();
		server.getDataSources().initFrom(dataSourceProperties.getDatasource().values());
		server.getApp().setName(properties.getName());
		server.getApp().setAlias(properties.getAlias());
		server.getApp().setVersion(properties.getVersion());
		return R.ok(server);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/dashboard")
	public R<?> getDashboardInfo() {
		DashboardServerData server = new DashboardServerData();
		server.getApp().setName(properties.getName());
		server.getApp().setAlias(properties.getAlias());
		server.getApp().setVersion(properties.getVersion());
		server.setStartTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getServerStartDate()));
		server.setRunTime(DateUtils.getDatePoor(DateUtils.getNowDate(), DateUtils.getServerStartDate()));
		return R.ok(server);
	}

	@Getter
	@Setter
	static class DashboardServerData {

		private AppInfo app = new AppInfo();

		private String startTime;

		private String runTime;
	}
}
