package com.chestnut.contentcore.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

/**
 * 资源存储策略
 */
@Component(IProperty.BEAN_NAME_PREFIX + FileStorageTypeProperty.ID)
public class FileStorageTypeProperty implements IProperty {

	public final static String ID = "FileStorageType";
	
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
		return "资源存储策略";
	}
	
	@Override
	public String defaultValue() {
		return LocalFileStorageType.TYPE;
	}
	
	public static String getValue(Map<String, String> props) {
		String value = ConfigPropertyUtils.getStringValue(ID, props);
		return StringUtils.isEmpty(value) ? LocalFileStorageType.TYPE : value;
	}
}
