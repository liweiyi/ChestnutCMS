package com.chestnut.contentcore.core;

/**
 * 栏目类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICatalogType {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "CatalogType_";

	/**
	 * 栏目类型ID
	 */
	String getId();
	
	/**
	 * 栏目类型名称
	 */
	String getName();
}
