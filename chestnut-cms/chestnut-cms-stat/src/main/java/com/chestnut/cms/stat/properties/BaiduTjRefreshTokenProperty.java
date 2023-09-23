package com.chestnut.cms.stat.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

@Component(IProperty.BEAN_NAME_PREFIX + BaiduTjRefreshTokenProperty.ID)
public class BaiduTjRefreshTokenProperty implements IProperty {

	public final static String ID = "BaiduTjRefreshToken";

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
		return "百度统计RefreshToken";
	}
	
	@Override
	public boolean isSensitive() {
		return true;
	}

	public static String getValue(Map<String, String> siteConfigProps) {
		return ConfigPropertyUtils.getStringValue(ID, siteConfigProps);
	}
}
