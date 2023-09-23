package com.chestnut.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.word.domain.TagWordGroup;

public interface ITagWordGroupService extends IService<TagWordGroup> {
	
	/**
	 * 添加TAG词分组
	 * 
	 * @param group
	 * @return
	 */
	TagWordGroup addTagWordGroup(TagWordGroup group);

	/**
	 * 编辑TAG词分组
	 * 
	 * @param group
	 * @return
	 */
	void editTagWordGroup(TagWordGroup group);

	/**
	 * 删除TAG词分组
	 * 
	 * @param groupIds
	 * @return
	 */
	void deleteTagWordGroups(List<Long> groupIds);

	/**
	 * 生成分组树数据
	 * 
	 * @param groups
	 * @return
	 */
	List<TreeNode<String>> buildTreeData(List<TagWordGroup> groups);
}
