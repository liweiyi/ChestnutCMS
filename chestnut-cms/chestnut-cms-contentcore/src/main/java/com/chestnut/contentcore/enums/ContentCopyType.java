package com.chestnut.contentcore.enums;

import java.util.Objects;

/**
 * 内容复制方式
 *
 */
public class ContentCopyType {

	/**
	 * 独立复制，完整拷贝内容所有信息，拷贝的内容变更与源内容无关，仅仅记录来源
	 */
	public static final int Independency = 1;
	
	/**
	 * 映射，仅拷贝基础内容信息，可独立修改基础信息，也就是CmsContent表的数据可独立修改，内容详情也就是扩展表共享自来源不可修改
	 */
	public static final int Mapping = 2;

	/**
	 * 是否独立复制内容
	 * 
	 * @param v
	 * @return
	 */
	public static boolean isIndependency(Integer v) {
		return Objects.nonNull(v) && v.intValue() == Independency;
	}
	
	/**
	 * 是否映射内容
	 *
	 * @param v
	 * @return
	 */
	public static boolean isMapping(Integer v) {
		return Objects.nonNull(v) && v.intValue() == Mapping;
	}
}
