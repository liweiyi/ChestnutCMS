package com.chestnut.exmodel.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

/**
 * 内容关联扩展模型
 */
@Component(IProperty.BEAN_NAME_PREFIX + ContentExtendModelProperty.ID)
public class ContentExtendModelProperty implements IProperty {

	public final static String ID = "ContentExtendModel";
	
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
		return "内容扩展模型";
	}
	
	public static String getValue(Map<String, String> props) {
		return ConfigPropertyUtils.getStringValue(ID, props);
	}
}
