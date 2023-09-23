package com.chestnut.system.fixed.config;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;

/**
 * 系统模块通用文件上传大小限制
 */
@Component(FixedConfig.BEAN_PREFIX + SysUploadSizeLimit.ID)
public class SysUploadSizeLimit extends FixedConfig {

	public static final String ID = "SysUploadSizeLimit";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	/**
	 * 默认上传文件大小限制：5M
	 */
	private static final long DEFAULT_VALUE = 5 * 1024 * 1024;
	
	public SysUploadSizeLimit() {
		super(ID, "{CONFIG." + ID + "}", String.valueOf(DEFAULT_VALUE), null);
	}
	
	public static void check(long fileSize) {
		String configValue = configService.selectConfigByKey(ID);
		long limit = ConvertUtils.toLong(configValue, DEFAULT_VALUE);
		Assert.isTrue(fileSize <= limit, () -> SysErrorCode.UPLOAD_FILE_SIZE_LIMIT.exception(limit));
	}
}
