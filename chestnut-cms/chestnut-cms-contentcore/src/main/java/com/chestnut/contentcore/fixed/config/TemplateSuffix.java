package com.chestnut.contentcore.fixed.config;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;

/**
 * 模板文件后缀名，默认：.template.html
 */
@Component(FixedConfig.BEAN_PREFIX + TemplateSuffix.ID)
public class TemplateSuffix extends FixedConfig {

	public static final String ID = "CMSTemplateSuffix";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	private static final String DEFAULT_VALUE = ".template.html";
	
	public TemplateSuffix() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
	
	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue)) {
			configValue = DEFAULT_VALUE;
		}
		return configValue;
	}
}
