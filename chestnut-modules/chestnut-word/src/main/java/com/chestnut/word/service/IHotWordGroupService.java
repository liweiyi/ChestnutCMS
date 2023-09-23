package com.chestnut.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.HotWordGroup;

public interface IHotWordGroupService extends IService<HotWordGroup> {

	/**
	 * 添加热词分组
	 * 
	 * @param group
	 */
	HotWordGroup addHotWordGroup(HotWordGroup group);

	/**
	 * 修改热词分组
	 * 
	 * @param group
	 */
	void updateHotWordGroup(HotWordGroup group);

	/**
	 * 删除热词分组
	 * 
	 * @param groupIds
	 */
	void deleteHotWordGroups(List<Long> groupIds);
}
