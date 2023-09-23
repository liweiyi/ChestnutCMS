package com.chestnut.advertisement.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdvertisementListener {

	private final IAdvertisementService advertisementService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除广告数据
		try {
			long total = this.advertisementService
					.count(new LambdaQueryWrapper<CmsAdvertisement>().eq(CmsAdvertisement::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除广告数据：" + (i * pageSize) + "/" + total);
				this.advertisementService.remove(new LambdaQueryWrapper<CmsAdvertisement>()
						.eq(CmsAdvertisement::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除广告数据错误：" + e.getMessage());
		}
	}
}
