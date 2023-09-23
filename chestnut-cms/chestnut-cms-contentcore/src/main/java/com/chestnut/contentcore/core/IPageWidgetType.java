package com.chestnut.contentcore.core;

import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.dto.PageWidgetAddDTO;
import com.chestnut.contentcore.domain.dto.PageWidgetEditDTO;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;

/**
 * 页面部件类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPageWidgetType {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "PageWidgetType_";

	/**
	 * 唯一标识：类型Id
	 */
	public String getId();
	
	/**
	 * 类型名
	 */
	public String getName();
	
	/**
	 * 类型图标
	 */
	public String getIcon();
	
	/**
	 * 获取路由地址
	 */
	public String getRoute();

	/**
	 * 加载部件数据详情
	 * 
	 * @param pageWidget
	 * @return
	 */
	public IPageWidget loadPageWidget(CmsPageWidget pageWidget);

	
	default public Class<?> getAddDTOClass() {
		return PageWidgetAddDTO.class;
	}

	default public Class<?> getEditDTOClass() {
		return PageWidgetEditDTO.class;
	}

	/**
	 * 页面部件数据转VO
	 * 
	 * @param pageWidget
	 * @return
	 */
	public PageWidgetVO getPageWidgetVO(CmsPageWidget pageWidget);

	/**
	 * 创建页面部件实例
	 * 
	 * @return
	 */
	public IPageWidget newInstance();

	/**
	 * 获取模板页面部件内容
	 * 
	 * @param content
	 * @param isPreview
	 * @return
	 */
	public Object parseContent(CmsPageWidget pageWidget, String publishPipeCode, boolean isPreview);
}
