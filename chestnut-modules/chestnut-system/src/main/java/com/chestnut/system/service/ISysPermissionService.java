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
package com.chestnut.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.permission.IPermissionOwnerType;
import com.chestnut.system.permission.IPermissionType;

import java.util.List;
import java.util.Set;

/**
 * 通用权限 业务层
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysPermissionService extends IService<SysPermission> {

	/** 所有权限标识 */
	String ALL_PERMISSION = "*";

	IPermissionType getPermissionType(String type);

	IPermissionOwnerType<?> getPermissionOwnerType(String ownerType);

	/**
	 * 获取权限信息
	 * 
	 * @param ownerType
	 * @param owner
	 * @return
	 */
	SysPermission getPermission(String ownerType, String owner, LoginUser operator);

	/**
	 * 保存权限数据
	 *
	 * @param ownerType
	 * @param owner
	 * @param perms
	 * @param permissionType
	 * @param operator
	 */
    void savePermissions(String ownerType, String owner, Set<String> perms, String permissionType, LoginUser operator);

	/**
	 * 获取指定类型权限列表
	 *
	 * @param ownerType
	 * @param owner
	 * @param permissionType
	 * @return
	 */
	Set<String> getPermissionKeys(String ownerType, String owner, String permissionType, LoginUser operator);

	/**ø
	 * 变更指定类型权限数据
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @param permissionJson 权限序列化值
	 */
    SysPermission setPermissionByType(String ownerType, String owner, String permissionType, String permissionJson);

	/**
	 * 授权
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @param permissionJson 权限序列化值
	 */
	SysPermission grantPermission(String ownerType, String owner, String permissionType, String permissionJson);

	/**
	 * 移除指定所有者权限数据
	 *
	 * @param ownerType 所有者类型
	 * @param owners 所有者标识列表
	 */
	void removePermissions(String ownerType, List<String> owners);
}
