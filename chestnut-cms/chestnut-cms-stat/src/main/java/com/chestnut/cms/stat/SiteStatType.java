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
package com.chestnut.cms.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.stat.core.IStatType;
import com.chestnut.stat.core.StatMenu;

@Component
public class SiteStatType implements IStatType {

	private final static List<StatMenu> STAT_MENU = List.of(
			new StatMenu("CmsSiteStat", "", "{STAT.MENU.CmsSiteStat}", 1),
			new StatMenu("BdSiteTrendOverview", "CmsSiteStat", "{STAT.MENU.BdSiteTrendOverview}", 1),
			new StatMenu("BdSiteTimeTrend", "CmsSiteStat", "{STAT.MENU.BdSiteTimeTrend}", 2),
			new StatMenu("CmsContentStat", "", "{STAT.MENU.CmsContentStat}", 2),
			new StatMenu("ContentDynamicStat", "CmsContentStat", "{STAT.MENU.ContentDynamicStat}", 1),
			new StatMenu("ContentStatByCatalog", "CmsContentStat", "{STAT.MENU.ContentStatByCatalog}", 2),
			new StatMenu("ContentStatByUser", "CmsContentStat", "{STAT.MENU.ContentStatByUser}", 3)
		);

	@Override
	public List<StatMenu> getStatMenus() {
		return STAT_MENU;
	}
}
