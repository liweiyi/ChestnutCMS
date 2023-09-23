package com.chestnut.advertisement;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.chestnut.advertisement.pojo.AdSpaceProps;
import com.chestnut.advertisement.pojo.vo.AdSpaceVO;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;

/**
 * 广告位页面部件
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPageWidgetType.BEAN_NAME_PREFIX + AdSpacePageWidgetType.ID)
public class AdSpacePageWidgetType implements IPageWidgetType {

	public final static String ID = "ads";
	public final static String NAME = "{CMS.CONTENCORE.PAGEWIDGET." + ID + "}";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getIcon() {
		return "el-icon-list";
	}
	
	@Override
	public String getRoute() {
		return "/cms/adspace/editor";
	}

	@Override
	public IPageWidget loadPageWidget(CmsPageWidget cmsPageWdiget) {
		AdSpacePageWidget pw = new AdSpacePageWidget();
		pw.setPageWidgetEntity(cmsPageWdiget);
		return pw;
	}
	
	@Override
	public IPageWidget newInstance() {
		return new AdSpacePageWidget();
	}
	
	@Override
	public PageWidgetVO getPageWidgetVO(CmsPageWidget pageWidget) {
		AdSpaceVO vo = new AdSpaceVO();
		BeanUtils.copyProperties(pageWidget, vo);
		vo.setContent(this.parseContent(pageWidget, null, true));
		return vo;
	}

	@Override
	public AdSpaceProps parseContent(CmsPageWidget pageWidget, String publishPipeCode, boolean isPreview) {
		return JacksonUtils.from(pageWidget.getContent(), AdSpaceProps.class);
	}
}
