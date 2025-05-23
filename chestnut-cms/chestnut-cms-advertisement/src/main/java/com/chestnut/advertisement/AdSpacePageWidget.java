/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
	}
}
