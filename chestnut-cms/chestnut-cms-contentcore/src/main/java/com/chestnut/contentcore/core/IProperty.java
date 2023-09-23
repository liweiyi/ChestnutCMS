package com.chestnut.contentcore.core;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.chestnut.common.utils.StringUtils;

/**
 * 扩展属性
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IProperty {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "CMSProperty_";
	
	/**
	 * 属性适用范围定义：站点、栏目
	 *
	 */
	enum UseType {
		Site, Catalog
	}

	/**
	 * 属性适用范围
	 * @return
	 */
	UseType[] getUseTypes();

	/**
	 * 属性值ID
	 */
	String getId();
	
	/**
	 * 属性值名称
	 */
	String getName();
	
	/**
	 * 校验使用类型
	 */
	default boolean checkUseType(UseType useType) {
		return ArrayUtils.contains(this.getUseTypes(), useType);
	}
	
	/**
	 * 属性值校验
	 */
	default boolean validate(String value) {
		return true;
	}
	
	/**
	 * 是否敏感数据
	 */
	default boolean isSensitive() {
		return false;
	}
	
	/**
	 * 属性默认值
	 */
	default Object defaultValue() {
		return StringUtils.EMPTY;
	}

	default Object getPropValue(Map<String, String> configProps) {
		return MapUtils.getString(configProps, getId(), defaultValue().toString());
	}
}
