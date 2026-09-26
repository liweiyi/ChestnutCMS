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
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.dto.CreateRoleRequest;
import com.chestnut.system.domain.dto.UpdateRoleRequest;
import com.chestnut.system.domain.dto.UpdateRoleStatusRequest;

import java.util.List;
import java.util.Set;

/**
 * 角色业务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysRoleService extends IService<SysRole> {

	/**
	 * 根据用户ID查询角色列表
	 * 
	 * @param userId 用户ID
	 * @return 角色列表
	 */
	List<SysRole> selectRolesByUserId(Long userId, String status);

	default List<SysRole> selectRolesByUserId(Long userId) {
		return selectRolesByUserId(userId, null);
	}

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
	void insertRole(CreateRoleRequest role);

	/**
	 * 修改保存角色信息
	 * 
	 * @param role 角色信息
	 */
	void updateRole(UpdateRoleRequest role);

	/**
     * 修改角色状态
     *
     * @param req 角色信息
     * @return
     */
	List<Long> updateRoleStatus(UpdateRoleStatusRequest req);

	/**
	 * 批量删除角色信息
	 * 
	 * @param roleIds 需要删除的角色ID
	 * @param operator 由 Controller 获取并传入的可信操作人，用于校验机构数据范围
	 * @return 受影响用户ID列表
	 */
	Set<Long> deleteRoleByIds(List<Long> roleIds, LoginUser operator);

	/**
	 * 获取缓存角色信息
	 * 
	 * @param roleId 角色ID
	 */
	SysRole getRole(Long roleId);

	/**
	 * 根据角色返回关联用户ID列表
	 *
	 * @param roleId 角色ID
	 * @return 用户ID列表
	 */
	List<Long> getUserIdsByRole(Long roleId);
}
