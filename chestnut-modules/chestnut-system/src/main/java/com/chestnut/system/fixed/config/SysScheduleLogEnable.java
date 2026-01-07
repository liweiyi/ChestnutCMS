/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

@Component(FixedConfig.BEAN_PREFIX + SysScheduleLogEnable.ID)
public class SysScheduleLogEnable extends FixedConfig {

	public static final String ID = "SysScheduleLogEnable";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	private static final String DEFAULT_VALUE = YesOrNo.NO;

	public SysScheduleLogEnable() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
	
	public static boolean isEnable() {
		String configValue = configService.selectConfigByKey(ID);
        return YesOrNo.isYes(configValue);
	}
}
