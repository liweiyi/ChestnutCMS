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
package com.chestnut.contentcore.publish.log;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.system.logs.ILogMenu;
import org.springframework.stereotype.Component;

@Component
public class PublishLogMenu implements ILogMenu {

	@Override
	public String getId() {
		return "Publish";
	}

	@Override
	public String getName() {
		return I18nUtils.get("{LOG.MENU." + getId() + "}");
	}

	@Override
	public String getRouter() {
		return "/monitor/logs/publish";
	}
}
