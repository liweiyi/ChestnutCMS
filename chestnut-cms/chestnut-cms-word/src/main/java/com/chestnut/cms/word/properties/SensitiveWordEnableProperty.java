package com.chestnut.cms.word.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

/**
 * 敏感词开关
 */
@Component(IProperty.BEAN_NAME_PREFIX + SensitiveWordEnableProperty.ID)
public class SensitiveWordEnableProperty implements IProperty {

	public final static String ID = "SensitiveWordEnable";
	
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
		return "是否开启文章保存敏感词替换";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.NO;
	}
	
	public static String getValue(Map<String, String> firstConfigProps, Map<String, String> secondConfigProps) {
		String value = ConfigPropertyUtils.getStringValue(ID, firstConfigProps, secondConfigProps);
		return YesOrNo.isYes(value) ? value : YesOrNo.NO;
	}
}
