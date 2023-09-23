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
