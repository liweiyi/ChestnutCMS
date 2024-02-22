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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysUserOnline;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysUserOnlineService;

import cn.dev33.satoken.session.SaSession;
import lombok.RequiredArgsConstructor;

/**
 * 在线用户监控
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseRestController {

	private final ISysUserOnlineService userOnlineService;

	private final RedisCache redisCache;
	
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorOnlineList)
	@GetMapping("/list")
	public R<?> list(String ipaddr, String userName) {
		String keyPrefix = StpAdminUtil.getStpLogic().getConfigOrGlobal().getTokenName() + ":" + AdminUserType.TYPE + ":token:";
		Set<String> keys = redisCache.scanKeys(keyPrefix + "*", 100);
		List<SysUserOnline> userOnlineList = keys.stream().map(key -> {
			SaSession tokenSession = StpAdminUtil.getTokenSessionByToken(key.substring(key.lastIndexOf(":") + 1));
			LoginUser loginUser = tokenSession.getModel(SaSession.USER, LoginUser.class);
			return userOnlineService.loginUserToUserOnline(loginUser);
		}).filter(online -> {
			if (Objects.isNull(online)) {
				return false;
			}
			if(StringUtils.isNotEmpty(ipaddr) && !StringUtils.equals(ipaddr, online.getIpaddr())) {
				return false;
			}
			if(StringUtils.isNotEmpty(userName) && !StringUtils.equals(userName, online.getUserName())) {
				return false;
			}
			return true;
		}).sorted((u1, u2) -> u1.getLoginTime() > u2.getLoginTime() ? 1 : -1).toList();
		return bindDataTable(userOnlineList);
	}

	/**
	 * 强退用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorOnlineForceLogout)
	@Log(title = "在线用户", businessType = BusinessType.FORCE)
	@DeleteMapping("/{tokenId}")
	public R<?> forceLogout(@PathVariable String tokenId) {
		StpAdminUtil.logoutByTokenValue(tokenId);
		return R.ok();
	}
}
