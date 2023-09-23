package com.chestnut.contentcore.core.impl;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.ICatalogType;

/**
 * 栏目类型：普通栏目
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(ICatalogType.BEAN_NAME_PREFIX + CatalogType_Common.ID)
public class CatalogType_Common implements ICatalogType {

	public final static String ID = "common";
    
    private final static String NAME = "{CMS.CONTENTCORE.CATALOG_TYPE." + ID + "}";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
