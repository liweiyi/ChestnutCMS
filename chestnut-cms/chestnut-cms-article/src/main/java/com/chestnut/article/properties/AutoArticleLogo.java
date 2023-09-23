package com.chestnut.article.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

/**
 * 自动提取文章正文首图作为Logo
 */
@Component(IProperty.BEAN_NAME_PREFIX + AutoArticleLogo.ID)
public class AutoArticleLogo implements IProperty {

	public final static String ID = "AutoArticleLogo";
	
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
		return "自动提取文章正文首图作为Logo";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.NO;
	}
	
	public static boolean getValue(Map<String, String> props) {
		return YesOrNo.isYes(ConfigPropertyUtils.getStringValue(ID, props));
	}
}
