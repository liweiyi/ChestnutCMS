package com.chestnut.advertisement;

/**
 * 广告素材类型
 */
public interface IAdvertisementType {

	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "AdvertisementType_";
	
	public String getId();
	
	public String getName();
}
