package com.chestnut.advertisement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.contentcore.core.AbstractPageWidget;

/**
 * 广告位
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class AdSpacePageWidget extends AbstractPageWidget {

	private final IAdvertisementService advertisementService = SpringUtils.getBean(IAdvertisementService.class);

	@Override
	public void delete() {
		super.delete();
		// 删除广告版位相关的广告
		this.advertisementService.remove(new LambdaQueryWrapper<CmsAdvertisement>()
				.eq(CmsAdvertisement::getAdSpaceId, this.getPageWidgetEntity().getPageWidgetId()));
		// TODO 删除广告统计数据
	}
}
