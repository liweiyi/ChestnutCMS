package com.chestnut.exmodel.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

/**
 * 站点扩展模型
 */
@Component(IProperty.BEAN_NAME_PREFIX + SiteExtendModelProperty.ID)
public class SiteExtendModelProperty implements IProperty {

	public final static String ID = "SiteExtendModel";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "站点扩展模型";
	}

	public static String getValue(Map<String, String> siteConfigProps) {
		return ConfigPropertyUtils.getStringValue(ID, siteConfigProps, null);
	}
}
