package com.chestnut.system.domain.vo;

import java.util.List;

import com.chestnut.common.domain.TreeNode;

/**
 * 角色菜单树
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public record RoleMenuTreeVO (List<TreeNode<Long>> menus, Integer total, List<Long> checkedKeys) {

}
