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
package com.chestnut.contentcore.fixed.config;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

/**
 * 允许上传的文件大小限制
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(FixedConfig.BEAN_PREFIX + ResourceUploadAcceptSize.ID)
public class ResourceUploadAcceptSize extends FixedConfig {

	public static final String ID = "ResourceUploadAcceptSize";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	/**
	 * 默认允许上传大小：20M，单位：byte
	 */
	public static final Long DEFAULT_ACCEPT_SIZE = 20 * 1024 * 1024L;

	public ResourceUploadAcceptSize() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_ACCEPT_SIZE.toString(), null);
	}

	public static Long getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue)) {
			return DEFAULT_ACCEPT_SIZE;
		}
		return Long.valueOf(configValue);
	}
}
