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
package com.chestnut.system.permission.impl;

import com.chestnut.system.domain.SysRole;
import com.chestnut.system.mapper.SysPermissionMapper;
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.permission.IPermissionOwnerType;
import com.chestnut.system.permission.IPermissionType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * 权限所有者类型：角色
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Order(100)
@RequiredArgsConstructor
@Component(IPermissionOwnerType.BEAN_PREFIX + RolePermissionOwnerType.TYPE)
public class RolePermissionOwnerType implements IPermissionOwnerType<SysRole> {

    public static final String TYPE = "Role";

    private final SysRoleMapper roleMapper;

    private final SysPermissionMapper permissionMapper;

    private final List<IPermissionType> permissionTypes;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public SysPermissionMapper getPermissionMapper() {
        return this.permissionMapper;
    }

    @Override
    public List<IPermissionType> getPermissionTypes() {
        return this.permissionTypes;
    }

    @Override
    public SysRole getOwnerInstance(Serializable owner) {
        return this.roleMapper.selectById(owner);
    }
}
