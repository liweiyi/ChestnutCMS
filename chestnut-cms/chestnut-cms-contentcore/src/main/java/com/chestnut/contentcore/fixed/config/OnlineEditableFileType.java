/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import java.util.Arrays;
import java.util.List;

/**
 * 允许在线编辑的文件类型
 */
@Component(FixedConfig.BEAN_PREFIX + OnlineEditableFileType.ID)
public class OnlineEditableFileType extends FixedConfig {

	public static final String ID = "OnlineEditableFileType";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	/**
	 * 默认允许在线编辑的文件类型
	 */
	public static final List<String> EXTENSION = List.of("txt", "shtml", "html", "htm", "js", "css", "json", "xml", "properties");

	public OnlineEditableFileType() {
		super(ID, "{CONFIG." + ID + "}", StringUtils.EMPTY,
				"默认支持：text,shtml,html,htm,js,css,json,xml,properties");
	}

	public static List<String> getAllowedExtensions() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue)) {
			return EXTENSION;
		}
		return Arrays.asList(configValue.split(","));
	}

	public static boolean  isAllow(String ext) {
		if (StringUtils.isEmpty(ext)) {
			return false;
		}
		if (ext.contains(".")) {
			ext = StringUtils.substringAfterLast(ext, ".");
		}
		return getAllowedExtensions().contains(ext);
	}
}
