package com.chestnut.cms.search.properties;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 是否开启索引
 */
@Component(IProperty.BEAN_NAME_PREFIX + EnableIndexProperty.ID)
public class EnableIndexProperty implements IProperty {

	public final static String ID = "EnableIndex";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site, UseType.Catalog };
	
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
		return "是否开启索引";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.YES;
	}
	
	public static String getValue(Map<String, String> firstConfigProps, Map<String, String> secondConfigProps) {
		String value = ConfigPropertyUtils.getStringValue(ID, firstConfigProps, secondConfigProps);
		return YesOrNo.isYes(value) ? value : YesOrNo.NO;
	}
}
