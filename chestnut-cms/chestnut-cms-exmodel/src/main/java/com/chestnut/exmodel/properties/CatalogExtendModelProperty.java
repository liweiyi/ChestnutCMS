package com.chestnut.exmodel.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

/**
 * 栏目扩展模型
 */
@Component(IProperty.BEAN_NAME_PREFIX + CatalogExtendModelProperty.ID)
public class CatalogExtendModelProperty implements IProperty {

	public final static String ID = "CatalogExtendModel";
	
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
		return "栏目扩展模型";
	}

	public static String getValue(Map<String, String> catalogConfigProps) {
		return ConfigPropertyUtils.getStringValue(ID, catalogConfigProps, null);
	}
}
