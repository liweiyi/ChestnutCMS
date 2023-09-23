package com.chestnut.system.fixed.config;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 后台访问地址
 */
@Component(FixedConfig.BEAN_PREFIX + BaiduMapAccessKey.ID)
public class BaiduMapAccessKey extends FixedConfig {

	public static final String ID = "BaiduMapAccessKey";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	public BaiduMapAccessKey() {
		super(ID, "{CONFIG." + ID + "}", StringUtils.EMPTY, null);
	}

	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		return Objects.isNull(configValue) ? StringUtils.EMPTY : configValue;
	}
}
