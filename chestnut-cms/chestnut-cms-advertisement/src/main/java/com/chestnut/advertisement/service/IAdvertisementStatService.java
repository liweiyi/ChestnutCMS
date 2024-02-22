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
	 * @param log 点击日志
	 */
	void adClick(CmsAdClickLog log);

	/**
	 * 广告展现
	 * 
	 * @param log 展现日志
	 */
	void adView(CmsAdViewLog log);
}
