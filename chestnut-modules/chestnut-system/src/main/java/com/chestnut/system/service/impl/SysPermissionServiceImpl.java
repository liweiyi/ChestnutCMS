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
package com.chestnut.system.service.impl;

import cn.dev33.satoken.session.SaSession;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.dto.SavePermissionRequest;
import com.chestnut.system.enums.PermissionOwnerType;
import com.chestnut.system.mapper.SysPermissionMapper;
import com.chestnut.system.permission.IPermissionType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysPermissionService;
import com.chestnut.system.service.ISysRoleService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final ISysRoleService roleService;

	private final Map<String, IPermissionType> permissionTypes;

	private IPermissionType getPermissionType(String type) {
		return this.permissionTypes.get(IPermissionType.BEAN_PREFIX + type);
	}

	@Override
	public SysPermission getPermission(String ownerType, String owner) {
		return this.lambdaQuery().eq(SysPermission::getOwnerType, ownerType).eq(SysPermission::getOwner, owner).one();
	}

	@Override
	public void savePermissions(String ownerType, String owner, Set<String> perms, String permissionType, String operator) {
		SysPermission permissions = this.getPermission(ownerType, owner);
		if (permissions == null) {
			permissions = new SysPermission();
			permissions.setPermId(IdUtils.getSnowflakeId());
			permissions.setOwnerType(ownerType);
			permissions.setOwner(owner);
			permissions.createBy(operator);
		}
		IPermissionType pt = getPermissionType(permissionType);
		permissions.getPermissions().put(pt.getId(), pt.serialize(perms));
		this.saveOrUpdate(permissions);
	}

	@Override
	public void saveMenuPermissions(SavePermissionRequest dto) {
		this.savePermissions(dto.getOwnerType(), dto.getOwner(), dto.getPermissions(), dto.getPermType(), dto.getOperator().getUserType());
	}

	@Override
	public Set<String> getPermissionKeys(String ownerType, String owner, String permissionType) {
		Set<String> permissionKeys = new HashSet<>();
		// 权限
		SysPermission userPermission = this.getPermission(ownerType, owner);
		if (Objects.nonNull(userPermission)) {
			this.permissionTypes.values().forEach(pt -> {
				if (StringUtils.isEmpty(permissionType) || pt.getId().equals(permissionType)) {
					String json = userPermission.getPermissions().get(pt.getId());
					if (StringUtils.isNotEmpty(json)) {
						permissionKeys.addAll(pt.deserialize(json));
					}
				}
			});
		}
		return permissionKeys;
	}

	@Override
	public Set<String> getUserPermissions(Long userId, @Nullable String permissionType) {
		Set<String> permissions = new HashSet<>();
		if (SecurityUtils.isSuperAdmin(userId)) {
			permissions.add(ALL_PERMISSION);
		} else {
			// 用户权限
			Set<String> permissionKeys = getPermissionKeys(PermissionOwnerType.User.name(), userId.toString(), permissionType);
			permissions.addAll(permissionKeys);
			// 角色权限
			List<SysRole> roles = this.roleService.selectRolesByUserId(userId);
			roles.forEach(r -> {
				Set<String> keys = getPermissionKeys(PermissionOwnerType.Role.name(), r.getRoleId().toString(), permissionType);
				permissions.addAll(keys);
			});
		}
		return permissions;
	}

	@Override
	public void resetLoginUserPermissions(LoginUser loginUser) {
		List<String> userPermissions = getUserPermissions(loginUser.getUserId(), null).stream().toList();
		loginUser.setPermissions(userPermissions);
		StpAdminUtil.getTokenValueListByLoginId(loginUser.getUserId()).forEach(token -> {
			SaSession session = StpAdminUtil.getTokenSessionByToken(token);
			LoginUser lu = (LoginUser) session.get(SaSession.USER);
			lu.setPermissions(userPermissions);
			session.set(SaSession.USER, loginUser);
		});
	}

	@Override
	public Set<String> getInheritedPermissionKeys(String ownerType, String owner, String permissionType) {
		Set<String> inheritedPermissionKeys = new HashSet<>();
		IPermissionType pt = getPermissionType(permissionType);
		if (PermissionOwnerType.isUser(ownerType)) {
			// 查找用户继承自角色的权限
			List<SysRole> roles = this.roleService.selectRolesByUserId(Long.valueOf(owner));
			if (!roles.isEmpty()) {
				List<SysPermission> rolePermissions = this.lambdaQuery()
						.eq(SysPermission::getOwnerType, PermissionOwnerType.Role.name())
						.in(SysPermission::getOwner, roles.stream().map(SysRole::getRoleId).toList())
						.list();
				rolePermissions.forEach(rolePermission -> {
					String json = rolePermission.getPermissions().get(pt.getId());
					inheritedPermissionKeys.addAll(pt.deserialize(json));
				});
			}
		}
		return inheritedPermissionKeys;
	}

	@Override
	public SysPermission setPermissionByType(String ownerType, String owner, String permissionType, String permissionJson) {
		SysPermission permission = this.getPermission(ownerType, owner);
		if (Objects.isNull(permission)) {
			permission = new SysPermission();
			permission.setPermId(IdUtils.getSnowflakeId());
			permission.setOwnerType(ownerType);
			permission.setOwner(owner);
			permission.createBy(SysConstants.SYS_OPERATOR);
			this.save(permission);
		}
		permission.getPermissions().put(permissionType, permissionJson);
		this.updateById(permission);
		return permission;
	}

	@Override
	public SysPermission grantUserPermission(LoginUser user, String permissionType, String permissionJson) {
		SysPermission permission = grantPermission(
				PermissionOwnerType.User.name(),
				user.getUserId().toString(),
				permissionType,
				permissionJson
		);
		this.resetLoginUserPermissions(user);
		return permission;
	}

	@Override
	public SysPermission grantPermission(String ownerType, String owner, String permissionType, String permissionJson) {
		SysPermission permission = this.getPermission(ownerType, owner);
		if (Objects.isNull(permission)) {
			permission = new SysPermission();
			permission.setPermId(IdUtils.getSnowflakeId());
			permission.setOwnerType(ownerType);
			permission.setOwner(owner);
			permission.createBy(SysConstants.SYS_OPERATOR);
			this.save(permission);
		}
		IPermissionType pt = getPermissionType(permissionType);
		Set<String> privs = pt.deserialize(permission.getPermissions().get(pt.getId()));
		privs.addAll(pt.deserialize(permissionJson));
		permission.getPermissions().put(permissionType, pt.serialize(privs));
		this.updateById(permission);
		return permission;
	}
}
