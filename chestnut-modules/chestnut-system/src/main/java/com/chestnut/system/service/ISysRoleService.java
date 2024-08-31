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
import com.chestnut.system.domain.SysRole;

import java.util.List;

/**
 * 角色业务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysRoleService extends IService<SysRole> {

	default List<SysRole> selectRolesByUserId(Long userId) {
		return selectRolesByUserId(userId, null);
	}

	/**
	 * 根据用户ID查询角色列表
	 * 
	 * @param userId 用户ID
	 * @return 角色列表
	 */
	List<SysRole> selectRolesByUserId(Long userId, String status);

	/**
	 * 根据用户ID查询角色权限
	 * 
	 * @param userId 用户ID
	 * @return 权限列表
	 */
	List<String> selectRoleKeysByUserId(Long userId);

	/**
	 * 新增保存角色信息
	 * 
	 * @param role 角色信息
	 */
	void insertRole(SysRole role);

	/**
	 * 修改保存角色信息
	 * 
	 * @param role 角色信息
	 */
	void updateRole(SysRole role);

	/**
	 * 修改角色状态
	 * 
	 * @param role 角色信息
	 */
	void updateRoleStatus(SysRole role);

	/**
	 * 批量删除角色信息
	 * 
	 * @param roleIds 需要删除的角色ID
	 */
	void deleteRoleByIds(List<Long> roleIds);

	/**
	 * 批量取消授权用户角色
	 * 
	 * @param roleId 角色ID
	 * @param userIds 需要取消授权的用户数据ID
	 */
	void deleteAuthUsers(Long roleId, List<Long> userIds);

	/**
	 * 批量选择授权用户角色
	 * 
	 * @param roleId 角色ID
	 * @param userIds 需要删除的用户数据ID
	 */
	void insertAuthUsers(Long roleId, List<Long> userIds);

	/**
	 * 获取缓存角色信息
	 * 
	 * @param roleCode 角色编码
	 */
	SysRole getRole(String roleCode);
}
