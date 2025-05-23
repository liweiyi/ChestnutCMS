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
package com.chestnut.contentcore.listener.event;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsCatalog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class OnCatalogMergeEvent extends ApplicationEvent {

	private CmsCatalog targetCatalog;

	private List<CmsCatalog> mergeCatalogs;

	private LoginUser operator;

	public OnCatalogMergeEvent(Object source, CmsCatalog targetCatalog, List<CmsCatalog> mergeCatalogs, LoginUser operator) {
		super(source);
		this.targetCatalog = targetCatalog;
		this.mergeCatalogs = mergeCatalogs;
		this.operator = operator;
	}
}
