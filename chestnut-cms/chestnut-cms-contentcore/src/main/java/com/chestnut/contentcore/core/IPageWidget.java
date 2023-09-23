package com.chestnut.contentcore.core;

import java.io.IOException;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsPageWidget;

import freemarker.template.TemplateException;

/**
 * 页面部件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPageWidget {

	/**
	 * 页面部件类型ID
	 */
	default String getPageWidgetType() {
		return this.getPageWidgetEntity().getType();
	}
	
	/**
	 * 页面部件基础数据实例
	 */
	public CmsPageWidget getPageWidgetEntity();

	public void setPageWidgetEntity(CmsPageWidget cmsPageWdiget);

	/**
	 * 操作人
	 * 
	 * @param loginUser
	 */
	public void setOperator(LoginUser loginUser);

	public LoginUser getOperator();

	public void add();

	public void save();

	public void delete();

	public void publish() throws TemplateException, IOException;
}
