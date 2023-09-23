package com.chestnut.advertisement.service;

import com.chestnut.advertisement.domain.CmsAdClickLog;
import com.chestnut.advertisement.domain.CmsAdViewLog;

/**
 * 广告数据管理Service
 */
public interface IAdvertisementStatService {

	/**
	 * 广告点击
	 * 
	 * @param advertisementIds
	 * @return
	 */
	public void adClick(CmsAdClickLog log);

	/**
	 * 广告展现
	 * 
	 * @param advertisementIds
	 * @return
	 */
	public void adView(CmsAdViewLog log);
}
