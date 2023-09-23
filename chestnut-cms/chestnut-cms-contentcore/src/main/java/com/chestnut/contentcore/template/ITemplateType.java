package com.chestnut.contentcore.template;

import com.chestnut.common.staticize.core.TemplateContext;

public interface ITemplateType {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "TemplateType_";

	/**
	 * 模板类型唯一标识
	 */
	String getId();

	/**
	 * 初始化当前模板类型的上下文变量
	 * 
	 * @param dataId
	 * @param context
	 */
	default void initTemplateData(Object dataId, TemplateContext context) {

	}
}
