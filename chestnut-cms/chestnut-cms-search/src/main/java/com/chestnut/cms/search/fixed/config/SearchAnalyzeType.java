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
package com.chestnut.cms.search.fixed.config;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.fixed.dict.WordAnalyzeType;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 内容索引分词策略
 */
@Component(FixedConfig.BEAN_PREFIX + SearchAnalyzeType.ID)
public class SearchAnalyzeType extends FixedConfig {

	public static final String ID = "CMSSearchAnalyzeType";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	public static final List<String> ALLOWED_LIST = List.of(
			WordAnalyzeType.IKMaxWord,
			WordAnalyzeType.IKSmart
	);

	public SearchAnalyzeType() {
		super(ID, "{CONFIG." + ID + "}", WordAnalyzeType.IKSmart,"支持：ik_smart/ik_max_word");
	}

	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue) || !ALLOWED_LIST.contains(configValue)) {
			return WordAnalyzeType.IKSmart;
		}
		return configValue;
	}
}
