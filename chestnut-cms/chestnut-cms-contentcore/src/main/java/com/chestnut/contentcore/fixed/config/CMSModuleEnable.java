package com.chestnut.contentcore.fixed.config;

import org.springframework.stereotype.Component;

import com.chestnut.system.fixed.FixedConfig;

/**
 * 是否开启CMS相关功能，主要是vue前端需要标志位来判断顶部站点切换控件的显示。
 */
@Component(FixedConfig.BEAN_PREFIX + CMSModuleEnable.ID)
public class CMSModuleEnable extends FixedConfig {

	public static final String ID = "CMSModuleEnable";
	
	private static final String DEFAULT_VALUE = "true";
	
	public CMSModuleEnable() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
}
