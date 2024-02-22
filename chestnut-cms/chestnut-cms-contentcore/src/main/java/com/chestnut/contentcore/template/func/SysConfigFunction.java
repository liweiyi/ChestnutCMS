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
package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.service.ISysConfigService;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：获取系统参数指定Key数据
 */
@Component
@RequiredArgsConstructor
public class SysConfigFunction extends AbstractFunc {

	private static final String FUNC_NAME = "sysConfig";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private final ISysConfigService sysConfigService;

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length != 1) {
			return null;
		}
		String configKey = args[0].toString();
		if (StringUtils.isBlank(configKey)) {
			return null;
		}
		return this.sysConfigService.selectConfigByKey(configKey);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("参数键名", FuncArgType.String, true, null));
	}
}
