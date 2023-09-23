package com.chestnut.contentcore.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

@Component(IProperty.BEAN_NAME_PREFIX + PublishedContentEditProperty.ID)
public class PublishedContentEditProperty implements IProperty {

	public final static String ID = "PublishedContentEdit";
	
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
		return "是否允许编辑已发布内容";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.YES;
	}
	
	public static boolean getValue(Map<String, String> props) {
		return YesOrNo.isYes(ConfigPropertyUtils.getStringValue(ID, props));
	}
}
