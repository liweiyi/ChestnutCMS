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
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.domain.dto.SysPermissionDTO;
import com.chestnut.system.permission.MenuPermissionType;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysMenuService;
import com.chestnut.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 权限配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
public class SysPermissionController extends BaseRestController {

	private final ISysPermissionService permissionService;

	private final ISysMenuService menuService;
	
	private final MenuPermissionType menuPermissionType;

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "权限设置", businessType = BusinessType.UPDATE)
	@PostMapping
	public R<?> saveMenuPermission(@Validated @RequestBody SysPermissionDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.permissionService.saveMenuPermissions(dto);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/menu")
	public R<?> getMenuPerms(@RequestParam String ownerType, @RequestParam String owner) {
		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();
		SysPermission permission = this.permissionService.getPermission(ownerType, owner);
		Set<String> perms = Set.of();
		if (Objects.nonNull(permission)) {
			String json  = permission.getPermissions().get(menuPermissionType.getId());
			perms = menuPermissionType.deserialize(json);
		}
		Set<String> disabledPerms = this.permissionService.getInheritedPermissionKeys(ownerType, owner, MenuPermissionType.ID);
		return R.ok(Map.of("menus", menus, "perms", perms, "disabledPerms", disabledPerms));
	}
}
