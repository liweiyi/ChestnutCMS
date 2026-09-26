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

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.domain.dto.SavePermissionRequest;
import com.chestnut.system.domain.vo.GetMenuPermissionVO;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.permission.impl.MenuPermissionType;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysMenuService;
import com.chestnut.system.service.ISysPermissionService;
import com.chestnut.system.service.impl.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 权限配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.PERMISSION.MODULE}")
@Priv(type = AdminUserType.TYPE, value = { SysMenuPriv.SysUserGrant, SysMenuPriv.SysRoleGrant },  mode = SaMode.OR)
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
public class SysPermissionController extends BaseRestController {

	private final ISysPermissionService permissionService;

	private final ISysMenuService menuService;
	
	private final MenuPermissionType menuPermissionType;

	private final UserPermissionService userPermissionService;

    @XComment("{API.DOC.SYS.PERMISSION.SAVE}")
	@Log(title = "权限设置", businessType = BusinessType.UPDATE)
	@PostMapping
	public R<Void> saveMenuPermission(@Validated @RequestBody SavePermissionRequest dto) {
		LoginUser operator = StpAdminUtil.getLoginUser();
		PermissionUtils.checkOwnerTypePermission(dto.getOwnerType(), operator);

		this.permissionService.savePermissions(dto.getOwnerType(), dto.getOwner(), dto.getPermissions(), dto.getPermType(), dto.getOperator());
		this.userPermissionService.resetLoginUser(dto.getOwnerType(), dto.getOwner());
		return R.ok();
	}

	@XComment("{API.DOC.SYS.PERMISSION.GET_MENU}")
	@GetMapping("/menu")
	public R<GetMenuPermissionVO> getMenuPerms(
			@RequestParam @XComment("{API.DOC.SYS.PERMISSION.OWNER_TYPE}") String ownerType,
			@RequestParam @XComment("{API.DOC.SYS.PERMISSION.OWNER}") String owner) {
        LoginUser operator = StpAdminUtil.getLoginUser();

		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();
        I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		SysPermission permission = this.permissionService.getPermission(ownerType, owner, operator);
		Set<String> perms = Set.of();
		if (Objects.nonNull(permission)) {
			String json  = permission.getPermissions().get(menuPermissionType.getId());
			perms = menuPermissionType.deserialize(json);
		}
		Set<String> disabledPerms = this.userPermissionService.getInheritedPermissionKeys(ownerType, owner, MenuPermissionType.ID);
		GetMenuPermissionVO vo = new GetMenuPermissionVO();
		vo.setMenus(menus);
		vo.setPerms(perms);
		vo.setDisabledPerms(disabledPerms);
		return R.ok(vo);
	}
}
