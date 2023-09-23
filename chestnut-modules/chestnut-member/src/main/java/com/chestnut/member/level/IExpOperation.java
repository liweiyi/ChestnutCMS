package com.chestnut.member.level;

/**
 * 等级经验变更操作项，经验值只增不减
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IExpOperation {
	
	String BEAN_PREFIX = "ExpOperation_";

	/**
	 * 唯一标识ID
	 */
	String getId();
	
	/**
	 * 操作项名称
	 */
	String getName();
}
