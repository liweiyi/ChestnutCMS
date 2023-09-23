package com.chestnut.vote.core;

/**
 * 问卷调查用户类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IVoteUserType {

	public String BEAN_PREFIX = "VoteUserType_";
	
	/**
	 * 问卷调查用户类型ID，唯一标识
	 */
	String getId();

	/**
	 * 问卷调查用户类型名称
	 */
	String getName();

	/**
	 * 获取用户唯一标识
	 */
	String getUserId();
}
