/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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

	/**
	 * 模板解析时间
	 */
	public static final String TemplateVariable_TimeMillis = "TimeMillis";
}
