package com.chestnut.advertisement.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.advertisement.IAdvertisementType;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.mapper.CmsAdvertisementMapper;
import com.chestnut.advertisement.pojo.dto.AdvertisementDTO;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.system.fixed.dict.EnableOrDisable;

import lombok.RequiredArgsConstructor;

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

	private static final String CACHE_KEY_ADV_IDS = CMSConfig.CachePrefix + "adv-ids";

	private final RedisCache redisCache;

	private final Map<String, IAdvertisementType> advertisementTypes;

	private final IPageWidgetService pageWidgetService;

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
		return this.redisCache.getCacheMap(CACHE_KEY_ADV_IDS,
				() -> this.lambdaQuery().select(List.of(CmsAdvertisement::getAdvertisementId, CmsAdvertisement::getName)).list()
						.stream().collect(
								Collectors.toMap(ad -> ad.getAdvertisementId().toString(), CmsAdvertisement::getName)));
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

		this.redisCache.deleteObject(CACHE_KEY_ADV_IDS);
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
	public void deleteAdvertisement(List<Long> advertisementIds) {
		this.removeByIds(advertisementIds);
		this.redisCache.deleteObject(CACHE_KEY_ADV_IDS);
	}

	@Override
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
	public void disableAdvertisement(List<Long> advertisementIds, String operator) {
		List<CmsAdvertisement> list = this.listByIds(advertisementIds);
		for (CmsAdvertisement ad : list) {
			if (ad.isEnable()) {
				ad.setState(EnableOrDisable.DISABLE);
				ad.updateBy(operator);
			}
		}
		this.updateBatchById(list);
		// todo 重新发布
	}
}
