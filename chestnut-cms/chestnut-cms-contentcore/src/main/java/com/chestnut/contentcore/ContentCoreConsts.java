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
package com.chestnut.contentcore;

public interface ContentCoreConsts {

	/**
	 * 资源预览地址前缀
	 */
	String RESOURCE_PREVIEW_PREFIX = "preview/";

	/**
	 * 缓存命名空间
	 */
	String CACHE_NAME = "cms";

	/**
	 * 栏目树最大层级，受限制于CmsCatalog.ancestors字段长度
	 */
	int CATALOG_MAX_TREE_LEVEL = 16;

	/**
	 * 当前站点Header键名
	 */
	String Header_CurrentSite = "CurrentSite";

	/**
	 * 模板目录，固定在站点发布通道目录下的template目录中
	 */
	String TemplateDirectory = "template/";

	/**
	 * 扩展配置属性字段模板变量前缀
	 */
	String ConfigPropFieldPrefix = "extend_";
}
