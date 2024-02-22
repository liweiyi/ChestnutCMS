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

import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.LoginBody;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.service.ISysLogininforService;
import com.chestnut.system.service.ISysMenuService;
import com.chestnut.system.service.ISysPermissionService;
import com.chestnut.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 登录验证
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
public class SysLoginController extends BaseRestController {

	private final SysLoginService loginService;

	private final ISysMenuService menuService;

	private final ISysRoleService roleService;

	private final ISysLogininforService logininfoService;

	/**
	 * 登录方法
	 * 
	 * @param loginBody 登录信息
	 * @return 结果
	 */
	@PostMapping("/login")
	public R<?> login(@Validated @RequestBody LoginBody loginBody) {
		// 生成令牌
		String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
				loginBody.getUuid());
		return R.ok(token);
	}

	@PostMapping("logout")
	public void logout() {
		try {
			if (StpAdminUtil.isLogin()) {
				LoginUser loginUser = StpAdminUtil.getLoginUser();
				StpAdminUtil.logout();
				this.logininfoService.recordLogininfor(loginUser.getUserType(),
						loginUser.getUserId(), loginUser.getUsername(), LoginLogType.LOGOUT, SuccessOrFail.SUCCESS,
						StringUtils.EMPTY);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 获取用户信息
	 * 
	 * @return 用户信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("getInfo")
	public R<?> getInfo() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
		// 角色集合
		List<String> roles = this.roleService.selectRoleKeysByUserId(user.getUserId());
		// 权限集合
		List<String> permissions = loginUser.getPermissions();
		return R.ok(Map.of("user", user, "roles", roles, "permissions", permissions));
	}

	/**
	 * 获取路由信息
	 * 
	 * @return 路由信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("getRouters")
	public R<?> getRouters() {
		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();

		List<String> permissions = StpAdminUtil.getLoginUser().getPermissions();
		if (!permissions.contains(ISysPermissionService.ALL_PERMISSION)) {
			menus = menus.stream().filter(m -> {
				return StringUtils.isEmpty(m.getPerms()) || permissions.contains(m.getPerms());
			}).toList();
		}
		// 国际化翻译
		I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		// 上下级关系处理
		menus = menuService.getChildPerms(menus, 0);
		return R.ok(menuService.buildRouters(menus));
	}
}
