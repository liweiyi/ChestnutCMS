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
package com.chestnut.contentcore.template.exception;

import com.chestnut.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateException;

public class CatalogNotFoundException extends TemplateException {

	private static final long serialVersionUID = 1L;

	public CatalogNotFoundException(String tag, long catalogId, String alias, Environment env) {
		super(StringUtils.messageFormat("<@{0}>[id: {1}, alias: {2}]", tag, catalogId, alias), env);
	}
}
