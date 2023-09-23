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
