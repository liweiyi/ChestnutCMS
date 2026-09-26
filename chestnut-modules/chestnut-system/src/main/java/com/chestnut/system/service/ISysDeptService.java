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

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.dto.CreateDeptRequest;
import com.chestnut.system.domain.dto.UpdateDeptRequest;

import java.util.List;
import java.util.function.Consumer;

/**
 * 部门管理 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysDeptService extends IService<SysDept> {

	/**
	 * 根据用户ID获取部门，范围用户所属部门及其下级部门，超级管理员返回所有部门
	 *
	 * @param userId 用户ID
	 * @return 部门列表
	 */
	default List<SysDept> getDepartmentsByUserId(Long userId) {
		return getDepartmentsByUserId(userId, null);
	}

	/**
	 * 根据用户ID获取部门，范围用户所属部门及其下级部门，超级管理员返回所有部门
	 *
	 * @param userId 用户ID
	 * @param consumer 查询条件
	 * @return 机构列表
	 */
	List<SysDept> getDepartmentsByUserId(Long userId, Consumer<LambdaQueryChainWrapper<SysDept>> consumer);

	/**
	 * 构建前端所需要的表格树结构
	 * 
	 * @param depts 部门列表
	 * @return 树结构列表
	 */
	List<SysDept> buildDeptTree(List<SysDept> depts);

	/**
	 * 新增保存部门信息
	 * 
	 * @param req 部门信息
	 */
	void insertDept(CreateDeptRequest req);

	/**
	 * 修改保存部门信息
	 * 
	 * @param req 部门信息
	 */
	void updateDept(UpdateDeptRequest req);

	/**
	 * 删除部门管理信息
	 * 
	 * @param deptId 部门ID
	 * @param operator 由 Controller 获取并传入的可信操作人，用于校验机构数据范围
	 */
	void deleteDeptById(Long deptId, LoginUser operator);

	/**
	 * 获取缓存部门信息
	 * 
	 * @param deptId 部门ID
	 * @return Optional<SysDept>
	 */
	SysDept getDept(Long deptId);

	/**
	 * 获取默认顶级机构
	 */
	SysDept getTopDept();
}
