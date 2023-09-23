package com.chestnut.cms.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chestnut.stat.IStatType;
import com.chestnut.stat.StatMenu;

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
