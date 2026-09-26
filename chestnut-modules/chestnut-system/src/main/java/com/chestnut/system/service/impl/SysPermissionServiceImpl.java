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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.mapper.SysPermissionMapper;
import com.chestnut.system.permission.IPermissionOwnerType;
import com.chestnut.system.permission.IPermissionType;
import com.chestnut.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final Map<String, IPermissionType> permissionTypes;

	private final Map<String, IPermissionOwnerType<?>> permissionOwnerTypes;

	@Override
	public IPermissionType getPermissionType(String type) {
		return this.permissionTypes.get(IPermissionType.BEAN_PREFIX + type);
	}

	@Override
	public IPermissionOwnerType<?> getPermissionOwnerType(String ownerType) {
		return this.permissionOwnerTypes.get(IPermissionOwnerType.BEAN_PREFIX + ownerType);
	}

	private void checkDeptScope(LoginUser operator, String ownerType, String owner) {
		IPermissionOwnerType<?> ot = this.getPermissionOwnerType(ownerType);
		ot.checkScope(operator, owner);
	}

	@Override
	public SysPermission getPermission(String ownerType, String owner, LoginUser operator) {
		this.checkDeptScope(operator, ownerType, owner);
		return this._getPermission(ownerType, owner);
	}

	private SysPermission _getPermission(String ownerType, String owner) {
		return this.lambdaQuery().eq(SysPermission::getOwnerType, ownerType).eq(SysPermission::getOwner, owner).one();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePermissions(String ownerType, String owner, Set<String> perms, String permissionType, LoginUser operator) {
		this.checkDeptScope(operator, ownerType, owner);

		SysPermission permissions = this._getPermission(ownerType, owner);
		if (permissions == null) {
			permissions = new SysPermission();
			permissions.setPermId(IdUtils.getSnowflakeId());
			permissions.setOwnerType(ownerType);
			permissions.setOwner(owner);
			permissions.createBy(operator.getUsername());
		}
		IPermissionType pt = this.getPermissionType(permissionType);
		permissions.getPermissions().put(pt.getId(), pt.serialize(perms));
		this.saveOrUpdate(permissions);
	}

	@Override
	public Set<String> getPermissionKeys(String ownerType, String owner, String permissionType, LoginUser operator) {
		this.checkDeptScope(operator, ownerType, owner);
		IPermissionOwnerType<?> ot = this.getPermissionOwnerType(ownerType);
		return ot.getPermissionKeys(owner, permissionType);
	}

	@Override
	public SysPermission setPermissionByType(String ownerType, String owner, String permissionType, String permissionJson) {
		SysPermission permission = this._getPermission(ownerType, owner);
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
	public SysPermission grantPermission(String ownerType, String owner, String permissionType, String permissionJson) {
		SysPermission permission = this._getPermission(ownerType, owner);
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

	@Override
	public void removePermissions(String ownerType, List<String> owners) {
		this.remove(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getOwnerType, ownerType)
				.in(SysPermission::getOwner, owners));
	}
}
