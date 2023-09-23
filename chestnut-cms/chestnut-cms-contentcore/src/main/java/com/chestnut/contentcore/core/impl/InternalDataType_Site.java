package com.chestnut.contentcore.core.impl;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.SiteUtils;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

/**
 * 内部数据类型：站点
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Site.ID)
public class InternalDataType_Site implements IInternalDataType {

	public final static String ID = "site";
	
	private final ISiteService siteService;
	
	private final IPublishService publishService;
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPageData(RequestData requestData) throws IOException, TemplateException {
		CmsSite site = siteService.getSite(requestData.getDataId());
		return this.publishService.getSitePageData(site, requestData.getPublishPipeCode(), requestData.isPreview());
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsSite site = siteService.getSite(internalUrl.getId());
		return SiteUtils.getSiteLink(site, publishPipeCode, isPreview);
	}
}
