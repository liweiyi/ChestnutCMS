package com.chestnut.system.fixed.config;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;

/**
 * 后台访问地址
 */
@Component(FixedConfig.BEAN_PREFIX + BackendContext.ID)
public class BackendContext extends FixedConfig {

	public static final String ID = "CMSBackendContext";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	private static final String DEFAULT_VALUE = "http://localhost/dev-api/";
	
	public BackendContext() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
	
	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue)) {
			configValue = DEFAULT_VALUE;
		}
		if (!configValue.endsWith("/")) {
			configValue += "/";
		}
		return configValue;
	}
}
