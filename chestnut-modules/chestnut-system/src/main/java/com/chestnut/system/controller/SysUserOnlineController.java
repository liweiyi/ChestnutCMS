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

import cn.dev33.satoken.session.SaSession;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysUserOnline;
import com.chestnut.system.domain.dto.QueryOnlineUserRequest;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysUserOnlineService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
	public R<?> list(@Validated QueryOnlineUserRequest req) {
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
			if(StringUtils.isNotEmpty(req.getIpaddr()) && !StringUtils.equals(req.getIpaddr(), online.getIpaddr())) {
				return false;
			}
            return !StringUtils.isNotEmpty(req.getUserName()) || StringUtils.equals(req.getUserName(), online.getUserName());
        }).sorted(Comparator.comparing(SysUserOnline::getLoginTime).reversed()).toList();
		return bindDataTable(userOnlineList);
	}

	/**
	 * 强退用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorOnlineForceLogout)
	@Log(title = "在线用户", businessType = BusinessType.FORCE)
	@PostMapping("/delete")
	public R<?> forceLogout(@RequestBody @NotEmpty List<String> tokenIds) {
		for (String tokenId : tokenIds) {
			StpAdminUtil.logoutByTokenValue(tokenId);
		}
		return R.ok();
	}
}
