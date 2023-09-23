package com.chestnut.cms.member.properties;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 是否允许投稿
 */
@Component(IProperty.BEAN_NAME_PREFIX + EnableContributeProperty.ID)
public class EnableContributeProperty implements IProperty {

	public final static String ID = "EnableContribute";
	
	static UseType[] UseTypes = new UseType[] { UseType.Catalog };
	
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
		return "是否允许投稿";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.NO;
	}
	
	public static boolean getValue(Map<String, String> configProps) {
		String value = ConfigPropertyUtils.getStringValue(ID, configProps);
		return YesOrNo.isYes(value);
	}
}
