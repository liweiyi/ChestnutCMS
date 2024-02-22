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

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.vo.RouterVO;

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
	public List<RouterVO> buildRouters(List<SysMenu> menus);

	/**
	 * 构建前端所需要树结构
	 * 
	 * @param menus
	 *            菜单列表
	 * @return 树结构列表
	 */
	public List<SysMenu> buildMenuTree(List<SysMenu> menus);

	/**
	 * 构建前端所需要下拉树结构
	 * 
	 * @param menus
	 *            菜单列表
	 * @return 下拉树结构列表
	 */
	public List<TreeNode<Long>> buildMenuTreeSelect(List<SysMenu> menus);

	/**
	 * 新增保存菜单信息
	 * 
	 * @param menu
	 *            菜单信息
	 * @return 结果
	 */
	public void insertMenu(SysMenu menu);

	/**
	 * 修改保存菜单信息
	 * 
	 * @param menu
	 *            菜单信息
	 * @return 结果
	 */
	public void updateMenu(SysMenu menu);

	/**
	 * 删除菜单管理信息
	 * 
	 * @param menuId
	 *            菜单ID
	 * @return 结果
	 */
	public void deleteMenuById(Long menuId);

	List<SysMenu> getChildPerms(List<SysMenu> list, int parentId);
}
