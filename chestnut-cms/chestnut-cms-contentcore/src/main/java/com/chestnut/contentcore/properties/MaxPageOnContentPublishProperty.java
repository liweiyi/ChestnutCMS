package com.chestnut.contentcore.properties;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

/**
 * 内容发布时更新内容列表页的最大页数
 */
@Component(IProperty.BEAN_NAME_PREFIX + MaxPageOnContentPublishProperty.ID)
public class MaxPageOnContentPublishProperty implements IProperty {

	public final static String ID = "MaxPageOnContentPublish";
	
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
		return "内容发布更新列表页数";
	}
	
	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || NumberUtils.isCreatable(value.toString());
	}
	
	@Override
	public Integer defaultValue() {
		return 5;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
		String string = MapUtils.getString(configProps, getId());
		if (NumberUtils.isCreatable(string)) {
			return NumberUtils.toInt(string);
		}
		return defaultValue();
	}
	
	public static int getValue(Map<String, String> props) {
		return ConfigPropertyUtils.getIntValue(ID, props);
	}
}
