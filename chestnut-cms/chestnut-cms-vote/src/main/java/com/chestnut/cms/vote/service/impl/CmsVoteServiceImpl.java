package com.chestnut.cms.vote.service.impl;

import org.springframework.stereotype.Service;

import com.chestnut.cms.vote.service.ICmsVoteService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CmsVoteServiceImpl implements ICmsVoteService {

	/**
	 * 获取问卷调查来源字段标识
	 * 
	 * @param siteId
	 * @return
	 */
	@Override
	public String getVoteSource(long siteId) {
		return "cms:" + siteId;
	}
}
