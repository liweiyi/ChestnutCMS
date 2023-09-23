package com.chestnut.contentcore.service;

import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.vo.SiteStatVO;

public interface ISiteStatService {

	/**
	 * 获取站点相关资源统计数据
	 * 
	 * @param site
	 * @param day
	 * @return
	 */
	SiteStatVO getSiteStat(CmsSite site);
}
