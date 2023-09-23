package com.chestnut.system.fixed.config;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;

@Component(FixedConfig.BEAN_PREFIX + SysRegistEnable.ID)
public class SysRegistEnable extends FixedConfig {

	public static final String ID = "SysRegistEnable";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	private static final String DEFAULT_VALUE = "false";
	
	public SysRegistEnable() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
	
	public static boolean isEnable() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isEmpty(configValue)) {
			return true;
		}
		return Boolean.parseBoolean(configValue);
	}
}
