package com.chestnut.contentcore.core;

import com.chestnut.contentcore.domain.CmsSite;

/**
 * 内容核心数据引用处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICoreDataHandler {

    default void onSiteExport(SiteExportContext context) {}

    default void onSiteImport(SiteImportContext context) {}
}
