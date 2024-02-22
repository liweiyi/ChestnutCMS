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
package com.chestnut.advertisement.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.stat.core.IStatType;
import com.chestnut.stat.core.StatMenu;

@Component
public class AdvertisementStatType implements IStatType {

	private final static List<StatMenu> STAT_MENU = List.of(new StatMenu("CmsAdv", "", "{STAT.MENU.CmsAdv}", 2),
			new StatMenu("CmsAdStat", "CmsAdv", "{STAT.MENU.CmsAdStat}", 1),
			new StatMenu("CmsAdClickLog", "CmsAdv", "{STAT.MENU.CmsAdClickLog}", 2),
			new StatMenu("CmsAdViewLog", "CmsAdv", "{STAT.MENU.CmsAdViewLog}", 3));

	@Override
	public List<StatMenu> getStatMenus() {
		return STAT_MENU;
	}
}
