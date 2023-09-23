package com.chestnut.advertisement.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.stat.IStatType;
import com.chestnut.stat.StatMenu;

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
