package com.chestnut.contentcore.template.impl;

import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.CatalogPageSizeProperty;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.ISiteService;
import org.springframework.stereotype.Component;

import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.util.TemplateUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + CatalogTemplateType.TypeId)
public class CatalogTemplateType implements ITemplateType {
	
	public final static String TypeId = "CatalogIndex";

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsCatalog catalog = this.catalogService.getCatalog(ConvertUtils.toLong(dataId));
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		TemplateUtils.addCatalogVariables(site, catalog, context);

		int pageSize = CatalogPageSizeProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		context.setPageSize(pageSize);
	}
}
