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
package com.chestnut.advertisement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.advertisement.AdSpacePageWidgetType;
import com.chestnut.advertisement.IAdvertisementType;
import com.chestnut.advertisement.cache.AdMonitoredCache;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.mapper.CmsAdvertisementMapper;
import com.chestnut.advertisement.pojo.dto.AdvertisementDTO;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.SiteApiUrlProperty;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.security.StpAdminUtil;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 广告数据服务实现类
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl extends ServiceImpl<CmsAdvertisementMapper, CmsAdvertisement>
		implements IAdvertisementService {

	private final AdMonitoredCache adCache;

	private final Map<String, IAdvertisementType> advertisementTypes;

	private final IPageWidgetService pageWidgetService;

	private final ISiteService siteService;

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public IAdvertisementType getAdvertisementType(String typeId) {
		return this.advertisementTypes.get(IAdvertisementType.BEAN_NAME_PREFIX + typeId);
	}

	@Override
	public List<IAdvertisementType> getAdvertisementTypeList() {
		return this.advertisementTypes.values().stream().toList();
	}

	@Override
	public Map<String, String> getAdvertisementMap() {
		return adCache.getCache(() -> {
			return this.lambdaQuery()
					.select(List.of(CmsAdvertisement::getAdvertisementId, CmsAdvertisement::getName))
					.list().stream()
					.collect(Collectors.toMap(ad -> ad.getAdvertisementId().toString(), CmsAdvertisement::getName));
		});
	}

	@Override
	public CmsAdvertisement addAdvertisement(AdvertisementDTO dto) {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(dto.getAdSpaceId());
		Assert.notNull(pageWidget,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("adSpaceId", dto.getAdSpaceId()));

		CmsAdvertisement advertisement = new CmsAdvertisement();
		BeanUtils.copyProperties(dto, advertisement);
		advertisement.setAdvertisementId(IdUtils.getSnowflakeId());
		advertisement.setSiteId(pageWidget.getSiteId());
		advertisement.setState(EnableOrDisable.ENABLE);
		advertisement.createBy(dto.getOperator().getUsername());
		this.save(advertisement);

		this.adCache.clear();
		return advertisement;
	}

	@Override
	public CmsAdvertisement saveAdvertisement(AdvertisementDTO dto) {
		CmsAdvertisement advertisement = this.getById(dto.getAdvertisementId());
		Assert.notNull(advertisement,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("advertisementId", dto.getAdvertisementId()));

		BeanUtils.copyProperties(dto, advertisement, "adSpaceId");
		advertisement.updateBy(dto.getOperator().getUsername());
		this.updateById(advertisement);
		return advertisement;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteAdvertisement(List<Long> advertisementIds) {
		this.removeByIds(advertisementIds);
		this.adCache.clear();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void enableAdvertisement(List<Long> advertisementIds, String operator) {
		List<CmsAdvertisement> list = this.listByIds(advertisementIds);
		for (CmsAdvertisement ad : list) {
			if (!ad.isEnable()) {
				ad.setState(EnableOrDisable.ENABLE);
				ad.updateBy(operator);
			}
		}
		this.updateBatchById(list);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void disableAdvertisement(List<Long> advertisementIds, String operator) {
		List<CmsAdvertisement> list = this.listByIds(advertisementIds);
		for (CmsAdvertisement ad : list) {
			if (ad.isEnable()) {
				ad.setState(EnableOrDisable.DISABLE);
				ad.updateBy(operator);
			}
		}
		this.updateBatchById(list);
		// 重新发布
		asyncTaskManager.execute(() -> {
			CmsPageWidget pageWidget = this.pageWidgetService.getById(list.get(0).getAdSpaceId());
			IPageWidgetType pwt = pageWidgetService.getPageWidgetType(AdSpacePageWidgetType.ID);
			IPageWidget pw = pwt.loadPageWidget(pageWidget);
			pw.setOperator(StpAdminUtil.getLoginUser());
			pw.publish();
        });
	}

	@Override
	public String getAdvertisementStatLink(CmsAdvertisement adv, String publishPipeCode) {
		CmsSite site = this.siteService.getSite(adv.getSiteId());
		String apiUrl = SiteApiUrlProperty.getValue(site, publishPipeCode);
		return apiUrl + "api/adv/redirect?sid=" + adv.getSiteId() + "&aid=" + adv.getAdvertisementId()
				+ "&url=" + URLEncoder.encode(adv.getRedirectUrl(), StandardCharsets.UTF_8);
	}
}
