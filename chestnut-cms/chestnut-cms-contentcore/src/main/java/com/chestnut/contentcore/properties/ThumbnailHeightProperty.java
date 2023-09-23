package com.chestnut.contentcore.properties;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 素材库图片默认缩略图高度配置
 *
 * @author 兮玥
 * @mail 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + ThumbnailHeightProperty.ID)
public class ThumbnailHeightProperty implements IProperty {

	public final static String ID = "ThumbnailHeight";

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
		return "素材库图片默认缩略图高度配置";
	}
	
	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || NumberUtils.isCreatable(value);
	}
	
	@Override
	public Integer defaultValue() {
		return 0;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
		String string = MapUtils.getString(configProps, getId());
		if (this.validate(string)) {
			return NumberUtils.toInt(string);
		}
		return defaultValue();
	}
	
	public static int getValue(Map<String, String> siteProps) {
		return ConfigPropertyUtils.getIntValue(ID, siteProps);
	}
}