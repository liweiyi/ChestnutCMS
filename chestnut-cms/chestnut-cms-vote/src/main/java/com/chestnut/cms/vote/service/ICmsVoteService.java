package com.chestnut.cms.vote.service;

public interface ICmsVoteService {

	/**
	 * 获取问卷调查来源字段标识
	 * 
	 * @param siteId
	 * @return
	 */
	String getVoteSource(long siteId);

}
