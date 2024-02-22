/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.cms.stat.service.impl;

import com.chestnut.cms.stat.baidu.BaiduTongjiConfig;
import com.chestnut.cms.stat.baidu.vo.BaiduSiteVO;
import com.chestnut.cms.stat.properties.BaiduTjAccessTokenProperty;
import com.chestnut.common.domain.R;
import com.chestnut.contentcore.service.ISiteService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import com.chestnut.cms.stat.baidu.BaiduTongjiUtils;
import com.chestnut.cms.stat.exception.CmsStatErrorCode;
import com.chestnut.cms.stat.properties.BaiduTjRefreshTokenProperty;
import com.chestnut.cms.stat.service.ICmsStatService;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.domain.CmsSite;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CmsStatServiceImpl implements ICmsStatService {

	private final ISiteService siteService;

	@Override
	public R<List<BaiduSiteVO>> getBaiduSiteList(CmsSite site) {
		BaiduTongjiConfig config = BaiduTongjiConfig.read(site.getConfigProps());
		R<List<BaiduSiteVO>> result = BaiduTongjiUtils.getSiteList(config);
		if (!result.isSuccess()) {
			if (result.getMsg().equalsIgnoreCase("Access token expired")) {
				refreshBaiduAccessToken(site);
				return BaiduTongjiUtils.getSiteList(config);
			}
		}
		return result;
	}

	/**
	 * 刷新百度统计AccessToken
	 * 
	 * @param site
	 * @return
	 */
	@Override
	public void refreshBaiduAccessToken(CmsSite site) {
		BaiduTongjiConfig config = BaiduTongjiConfig.read(site.getConfigProps());
		Assert.isTrue(config.validate(), CmsStatErrorCode.BAIDU_TONGJI_CONFIG_EMPTY::exception);

		Map<String, Object> result = BaiduTongjiUtils.refreshAccessToken(config);
		String accessToken = MapUtils.getString(result, "access_token");
		String _refreshToken = MapUtils.getString(result, "refresh_token");
		site.getConfigProps().put(BaiduTjAccessTokenProperty.ID, accessToken);
		site.getConfigProps().put(BaiduTjRefreshTokenProperty.ID, _refreshToken);
		this.siteService.updateById(site);
		this.siteService.clearCache(site.getSiteId());
	}
}
