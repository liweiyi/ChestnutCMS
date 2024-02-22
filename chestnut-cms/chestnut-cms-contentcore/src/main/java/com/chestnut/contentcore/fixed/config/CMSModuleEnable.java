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

import org.springframework.stereotype.Component;

import com.chestnut.system.fixed.FixedConfig;

/**
 * 是否开启CMS相关功能，主要是vue前端需要标志位来判断顶部站点切换控件的显示。
 */
@Component(FixedConfig.BEAN_PREFIX + CMSModuleEnable.ID)
public class CMSModuleEnable extends FixedConfig {

	public static final String ID = "CMSModuleEnable";
	
	private static final String DEFAULT_VALUE = "true";
	
	public CMSModuleEnable() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
}
