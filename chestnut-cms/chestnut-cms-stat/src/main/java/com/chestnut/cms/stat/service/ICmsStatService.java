package com.chestnut.cms.stat.service;

import com.chestnut.cms.stat.baidu.vo.BaiduSiteVO;
import com.chestnut.common.domain.R;
import com.chestnut.contentcore.domain.CmsSite;

import java.util.List;

public interface ICmsStatService {

	R<List<BaiduSiteVO>> getBaiduSiteList(CmsSite site);

	/**
	 * 刷新百度统计AccessToken
	 * 
	 * @param site
	 * @return
	 */
	void refreshBaiduAccessToken(CmsSite site);
}
