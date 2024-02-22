/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
