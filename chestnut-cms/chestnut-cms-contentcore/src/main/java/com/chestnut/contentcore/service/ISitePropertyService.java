package com.chestnut.contentcore.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.domain.R;
import com.chestnut.contentcore.domain.CmsSiteProperty;

public interface ISitePropertyService extends IService<CmsSiteProperty> {

	R<String> addSiteProperty(CmsSiteProperty siteProperty);

	R<String> saveSiteProperty(CmsSiteProperty siteProperty);

	R<String> deleteSiteProperties(List<Long> propertyIds);
}
