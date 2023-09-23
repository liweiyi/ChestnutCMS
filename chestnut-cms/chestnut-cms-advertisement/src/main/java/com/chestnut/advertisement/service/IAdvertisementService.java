package com.chestnut.advertisement.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.advertisement.IAdvertisementType;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.pojo.dto.AdvertisementDTO;

/**
 * 广告数据管理Service
 */
public interface IAdvertisementService extends IService<CmsAdvertisement> {

	/**
	 * 广告<ID, NAME>缓存集合
	 * 
	 * @return
	 */
	Map<String, String> getAdvertisementMap();
	
	/**
	 * 添加广告数据
	 * 
	 * @param pw
	 * @return
	 * @throws IOException
	 */
	public CmsAdvertisement addAdvertisement(AdvertisementDTO dto);
	
	/**
	 * 修改广告数据
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	public CmsAdvertisement saveAdvertisement(AdvertisementDTO dto);
	
	/**
	 * 删除广告数据
	 * 
	 * @param advertisementIds
	 * @return
	 * @throws IOException
	 */
	public void deleteAdvertisement(List<Long> advertisementIds);

	/**
	 * 获取广告类型
	 * 
	 * @param typeId
	 * @return
	 */
	public IAdvertisementType getAdvertisementType(String typeId);

	/**
	 * 广告类型列表
	 * 
	 * @return
	 */
	public List<IAdvertisementType> getAdvertisementTypeList();

	/**
	 * 启用广告
	 * 
	 * @param advertisementIds
	 * @return
	 */
	public void enableAdvertisement(List<Long> advertisementIds, String operator);

	/**
	 * 停用广告
	 * 
	 * @param advertisementIds
	 * @return
	 */
	public void disableAdvertisement(List<Long> advertisementIds, String operator);
}
