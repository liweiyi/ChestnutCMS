package com.chestnut.common.staticize;

public class StaticizeConstants {

	/**
	 * 通用模板变量：标签数据集
	 */
	public final static String TemplateVariable_Data = "Data";

	/**
	 * 模板变量：循环标签数据集
	 */
	public final static String TemplateVariable_DataList = "DataList";

	/**
	 * 模板全局变量：分页标签数据总数
	 */
	public static final String TemplateVariable_PageTotal = "PageTotal";

	/**
	 * 模板全局变量：分页标签每页数据量
	 */
	public static final String TemplateVariable_PageSize = "PageSize";

	/**
	 * 模板全局变量：分页标签当前页码
	 */
	public static final String TemplateVariable_PageNo = "PageNo";

	/**
	 * 模板全局变量：第一页文件名
	 */
	public static final String TemplateVariable_FirstPage = "FirstPage";

	/**
	 * 模板全局变量：其他页文件名，带{PageNo占位符}
	 */
	public static final String TemplateVariable_OtherPage = "OtherPage";

	/**
	 * 模板变量：自定义上下文
	 */
	public static final String TemplateVariable_TemplateContext = "TemplateContext";
}
