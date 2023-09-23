package com.chestnut.contentcore.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

/**
 * 是否开启图片水印
 */
@Component(IProperty.BEAN_NAME_PREFIX + ImageWatermarkProperty.ID)
public class ImageWatermarkProperty implements IProperty {

	public final static String ID = "ImageWatermark";

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
		return "图片水印";
	}

	@Override
	public String defaultValue() {
		return YesOrNo.NO;
	}
	
	public static boolean getValue(Map<String, String> props) {
		return YesOrNo.isYes(ConfigPropertyUtils.getStringValue(ID, props));
	}
}
