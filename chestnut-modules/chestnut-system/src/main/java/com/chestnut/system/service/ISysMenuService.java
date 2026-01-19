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

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.dto.CreateMenuRequest;
import com.chestnut.system.domain.dto.UpdateMenuRequest;
import com.chestnut.system.domain.vo.RouterVO;

import java.util.List;

/**
 * 菜单 业务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysMenuService extends IService<SysMenu> {

	/**
	 * 构建前端路由所需要的菜单
	 * 
	 * @param menus
	 *            菜单列表
	 * @return 路由列表
	 */
	List<RouterVO> buildRouters(List<SysMenu> menus);

	/**
	 * 构建前端所需要树结构
	 * 
	 * @param menus
	 *            菜单列表
	 * @return 树结构列表
	 */
	List<SysMenu> buildMenuTree(List<SysMenu> menus);

	/**
	 * 构建前端所需要下拉树结构
	 * 
	 * @param menus
	 *            菜单列表
	 * @return 下拉树结构列表
	 */
	List<TreeNode<Long>> buildMenuTreeSelect(List<SysMenu> menus);

	/**
	 * 新增保存菜单信息
	 * 
	 * @param req 菜单信息
	 */
	void insertMenu(CreateMenuRequest req);

	/**
	 * 修改保存菜单信息
	 *
	 * @param req 菜单信息
	 */
	void updateMenu(UpdateMenuRequest req);

	/**
	 * 删除菜单管理信息
	 * 
	 * @param menuId
	 *            菜单ID
	 * @return 结果
	 */
	void deleteMenuById(Long menuId);

	/**
	 * 根据父节点的ID获取所有子节点
	 *
	 * @param list     分类表
	 * @param parentId 传入的父节点ID
	 * @return String
	 */
	List<SysMenu> getChildPerms(List<SysMenu> list, int parentId);
}
