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
package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.domain.dto.SysPermissionDTO;
import jakarta.annotation.Nullable;

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

	/**
	 * 获取权限信息
	 * 
	 * @param ownerType
	 * @param owner
	 * @return
	 */
	SysPermission getPermission(String ownerType, String owner);

	/**
	 * 保存权限数据
	 *
	 * @param ownerType
	 * @param owner
	 * @param perms
	 * @param permissionType
	 * @param operator
	 */
    void savePermissions(String ownerType, String owner, Set<String> perms, String permissionType, String operator);

    /**
	 * 保存菜单权限信息
	 * 
	 * @param dto
	 */
	void saveMenuPermissions(SysPermissionDTO dto);

	/**
	 * 获取指定类型权限列表
	 *
	 * @param ownerType
	 * @param owner
	 * @param permissionType
	 * @return
	 */
	Set<String> getPermissionKeys(String ownerType, String owner, String permissionType);

	/**
	 * 获取用户权限列表
	 * 
	 * @param userId
	 * @param permissionType 指定权限类型
	 * @return
	 */
	Set<String> getUserPermissions(Long userId, @Nullable String permissionType);

	/**
	 * 重置登录用户权限信息
	 *
	 * @param loginUser 登录用户信息
	 */
	void resetLoginUserPermissions(LoginUser loginUser);

	/**
	 * 获取继承权限
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @return
	 */
	Set<String> getInheritedPermissionKeys(String ownerType, String owner, String permissionType);

	/**
	 * 变更指定类型权限数据
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @param permissionJson 权限序列化值
	 */
    SysPermission setPermissionByType(String ownerType, String owner, String permissionType, String permissionJson);

	/**
	 * 用户授权变更，更新Token权限
	 *
	 * @param user 登录用户Token
	 * @param permissionType 权限类型
	 * @param permissionJson 权限序列化值
	 */
	SysPermission grantUserPermission(LoginUser user, String permissionType, String permissionJson);

	/**
	 * 授权
	 *
	 * @param ownerType 权限所有者类型
	 * @param owner 权限所有者唯一标识
	 * @param permissionType 权限类型
	 * @param permissionJson 权限序列化值
	 */
	SysPermission grantPermission(String ownerType, String owner, String permissionType, String permissionJson);
}
