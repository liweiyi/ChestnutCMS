package com.chestnut.contentcore.core;

import java.util.List;

import com.chestnut.common.utils.StringUtils;

/**
 * 发布通道属性
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPublishPipeProp {
	
	String DetailTemplatePropPrefix = "detailTemplate_";
	
	String DefaultDetailTemplatePropPrefix = "defaultDetailTemplate_";

	/**
	 * 属性唯一标识键名
	 */
	String getKey();
	
	/**
	 * 属性名称
	 */
	String getName();
	
	/**
	 * 属性应用类型
	 */
	List<PublishPipePropUseType> getUseTypes();
	
	/**
	 * 默认值
	 */
	default String getDefaultValue() {
		return StringUtils.EMPTY;
	}
	
	/**
	 * 应用类型
	 */
	enum PublishPipePropUseType {
		Site, Catalog, Content
	}
}
