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
import com.chestnut.system.domain.dto.SysPermissionDTO;
import com.chestnut.system.enums.PermissionOwnerType;
import com.chestnut.system.mapper.SysPermissionMapper;
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.permission.IPermissionType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysPermissionService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final SysRoleMapper roleMapper;

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
	public void saveMenuPermissions(SysPermissionDTO dto) {
		this.savePermissions(dto.getOwnerType(), dto.getOwner(), dto.getPermissions(), dto.getPermType(), dto.getOperator().getUserType());
	}

	@Override
	public Set<String> getUserPermissions(Long userId, @Nullable String permissionType) {
		Set<String> permissions = new HashSet<>();
		if (SecurityUtils.isSuperAdmin(userId)) {
			permissions.add(ALL_PERMISSION);
		} else {
			// 用户权限
			SysPermission userPermission = this.getPermission(PermissionOwnerType.User.name(), userId.toString());
			if (userPermission != null) {
				this.permissionTypes.values().forEach(pt -> {
					if (StringUtils.isEmpty(permissionType) || pt.getId().equals(permissionType)) {
						String json = userPermission.getPermissions().get(pt.getId());
						if (StringUtils.isNotEmpty(json)) {
							permissions.addAll(pt.convert(json));
						}
					}
				});
			}
			// 角色权限
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
			roles.forEach(r -> {
				SysPermission permission = this.getPermission(PermissionOwnerType.Role.name(),
						r.getRoleId().toString());
				if (permission != null) {
					this.permissionTypes.values().forEach(pt -> {
						if (StringUtils.isEmpty(permissionType) || pt.getId().equals(permissionType)) {
							String json = permission.getPermissions().get(pt.getId());
							if (StringUtils.isNotEmpty(json)) {
								permissions.addAll(pt.convert(json));
							}
						}
					});
				}
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
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(Long.valueOf(owner));
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
		permission.getPermissions().put(permissionType, permissionJson);
		this.updateById(permission);
		return permission;
	}
}
