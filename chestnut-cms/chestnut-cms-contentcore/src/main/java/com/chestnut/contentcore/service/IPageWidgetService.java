package com.chestnut.contentcore.service;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;

import freemarker.template.TemplateException;

public interface IPageWidgetService extends IService<CmsPageWidget> {

	/**
	 * 获取指定页面部件类型
	 */
	IPageWidgetType getPageWidgetType(String type);

	/**
	 * 获取页面部件类型列表
	 */
	List<IPageWidgetType> getPageWidgetTypes();
	
	/**
	 * 添加页面部件数据
	 */
	void addPageWidget(IPageWidget pw);
	
	/**
	 * 修改页面部件数据
	 */
	void savePageWidget(IPageWidget pw);
	
	/**
	 * 删除页面部件数据
	 */
	void deletePageWidgets(List<Long> pageWidgetIds, LoginUser operator);

	/**
	 * 删除栏目相关页面部件数据
	 * 
	 * @param catalog
	 */
	void deletePageWidgetsByCatalog(CmsCatalog catalog);

	/**
	 * 发布页面部件
	 */
	void publishPageWidgets(List<Long> pageWidgetId, LoginUser operator) throws TemplateException, IOException;

	/**
	 * 是否存在重复编码
	 */
	boolean checkCodeUnique(Long siteId, String code, Long pageWidgetId);

	/**
	 * 获取页面部件缓存数据
	 *
	 * @param code
	 * @return
	 */
    CmsPageWidget getPageWidget(Long siteId, String code);
}
