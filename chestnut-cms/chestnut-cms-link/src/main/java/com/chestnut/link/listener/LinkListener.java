package com.chestnut.link.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.domain.CmsLinkGroup;
import com.chestnut.link.service.ILinkGroupService;
import com.chestnut.link.service.ILinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
}
