package com.chestnut.link.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.listener.event.SiteThemeExportEvent;
import com.chestnut.contentcore.listener.event.SiteThemeImportEvent;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.domain.CmsLinkGroup;
import com.chestnut.link.service.ILinkGroupService;
import com.chestnut.link.service.ILinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkListener {

	private final ILinkGroupService linkGroupService;

	private final ILinkService linkService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		try {
			// 删除友链数据
			long total = this.linkService
					.count(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除友链数据：" + (i * pageSize) + "/" + total);
				this.linkService.remove(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getSiteId, site.getSiteId())
						.last("limit " + pageSize));
			}
			// 删除友链分组数据
			total = this.linkGroupService
					.count(new LambdaQueryWrapper<CmsLinkGroup>().eq(CmsLinkGroup::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除友链分组数据：" + (i * pageSize) + "/" + total);
				this.linkGroupService.remove(new LambdaQueryWrapper<CmsLinkGroup>()
						.eq(CmsLinkGroup::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除友链数据错误：" + e.getMessage());
		}
	}

	@EventListener
	public void onSiteThemeImport(SiteThemeImportEvent event) throws IOException {
		int percent = AsyncTaskManager.getTaskProgressPercent();
		AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
				"正在导入友情链接分组数据");
		// cms_link_group
		File dataFile = new File(event.getDestDir() + "db/" + CmsLinkGroup.TABLE_NAME + ".json");
		if (dataFile.exists()) {
			List<CmsLinkGroup> list = JacksonUtils.fromList(dataFile, CmsLinkGroup.class);
			for (CmsLinkGroup data : list) {
				try {
					CmsLinkGroup linkGroup = new CmsLinkGroup();
					linkGroup.setSiteId(event.getSite().getSiteId());
					linkGroup.setLinkGroupId(IdUtils.getSnowflakeId());
					linkGroup.setName(data.getName());
					linkGroup.setCode(data.getCode());
					linkGroup.setSortFlag(SortUtils.getDefaultSortValue());
					linkGroup.createBy(event.getOperator().getUsername());
					linkGroupService.save(linkGroup);
				} catch (Exception e) {
					AsyncTaskManager.addErrMessage("导入友链分组数据添加失败：" + data.getName() + "|" + data.getCode() + " > " + e.getMessage());
				}
			}
		}
	}

	@EventListener
	public void onSiteThemeExport(SiteThemeExportEvent event) throws IOException {
		// cms_link_group
		int percent = AsyncTaskManager.getTaskProgressPercent();
		AsyncTaskManager.setTaskProgressInfo( percent + (100 - percent) / 10,
				"正在导出友情链接分组数据");
		List<CmsLinkGroup> list = linkGroupService.lambdaQuery()
				.eq(CmsLinkGroup::getSiteId, event.getSite().getSiteId())
				.list();
		String json = JacksonUtils.to(list);
		event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
				.path("db/" + CmsLinkGroup.TABLE_NAME + ".json")
				.save();
	}
}
