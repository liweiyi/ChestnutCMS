package com.chestnut.stat.service;

import java.util.List;

import com.chestnut.common.domain.TreeNode;
import com.chestnut.stat.IStatType;

public interface IStatService {

	/**
	 * 获取指定统计菜单类型
	 * 
	 * @param typeId
	 * @return
	 */
	IStatType getStatType(String typeId);

	/**
	 * 获取统计菜单类型列表
	 */
	List<IStatType> getStatTypes();

	/**
	 * 获取统计菜单树
	 * 
	 * @return
	 */
	List<TreeNode<String>> getStatMenuTree();
}
