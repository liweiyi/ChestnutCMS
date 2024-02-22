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

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.system.domain.SysDept;

/**
 * 部门管理 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysDeptService extends IService<SysDept> {

	/**
	 * 构建前端所需要的表格树结构
	 * 
	 * @param depts
	 *            部门列表
	 * @return 树结构列表
	 */
	public List<SysDept> buildDeptTree(List<SysDept> depts);

	/**
	 * 构建前端所需要下拉树结构
	 * 
	 * @param depts
	 *            部门列表
	 * @return 下拉树结构列表
	 */
	public List<TreeNode<Long>> buildDeptTreeSelect(List<SysDept> depts);

	/**
	 * 新增保存部门信息
	 * 
	 * @param dept
	 *            部门信息
	 * @return 结果
	 */
	public void insertDept(SysDept dept);

	/**
	 * 修改保存部门信息
	 * 
	 * @param dept
	 *            部门信息
	 * @return 结果
	 */
	public void updateDept(SysDept dept);

	/**
	 * 删除部门管理信息
	 * 
	 * @param deptId
	 *            部门ID
	 * @return 结果
	 */
	public void deleteDeptById(Long deptId);

	/**
	 * 获取缓存部门信息
	 * 
	 * @param deptId
	 * @return
	 */
	Optional<SysDept> getDept(Long deptId);
}
