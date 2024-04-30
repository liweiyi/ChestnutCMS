package com.chestnut.stat;

import java.util.List;

/**
 * 统计类型
 * 
 * <p>
 * 每一个统计类型对应一种统计数据的存储及展示，前端根据统计类型进行分类展示
 * </p>
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IStatType {
	
	/**
	 * 统计菜单树
	 */
	public List<StatMenu> getStatMenus();
}