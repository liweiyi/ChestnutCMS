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
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;

/**
 * 系统模块通用文件上传类型限制
 */
@Component(FixedConfig.BEAN_PREFIX + SysUploadTypeLimit.ID)
public class SysUploadTypeLimit extends FixedConfig {

	public static final String ID = "SysUploadTypeLimit";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	/**
	 * 默认上传文件类型
	 */
	private static final String[] DEFAULT_VALUE = { "jpg", "jpeg", "gif", "png", "xls", "xlsx", "doc", "docx", "ppt", "pptx", "pdf", "html", "txt", "zip" };
	
	public SysUploadTypeLimit() {
		super(ID, "{CONFIG." + ID + "}", String.join(",", DEFAULT_VALUE), null);
	}
	
	public static void check(String ext) {
		boolean flag;
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isEmpty(configValue)) {
			flag = StringUtils.containsAnyIgnoreCase(ext, DEFAULT_VALUE);
		} else {
			flag = StringUtils.containsAnyIgnoreCase(ext, configValue.split(","));
		}
		Assert.isTrue(flag, () -> SysErrorCode.UPLOAD_FILE_TYPE_LIMIT.exception());
	}
}
