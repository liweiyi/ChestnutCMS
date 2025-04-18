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

import java.util.Map;

import org.springframework.context.ApplicationEvent;

import com.chestnut.contentcore.domain.CmsCatalog;

import lombok.Getter;

@Getter
public class AfterCatalogSaveEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private String oldPath;

	private Map<String, Object> extendParams;

	private CmsCatalog catalog;

	public AfterCatalogSaveEvent(Object source, CmsCatalog catalog, String oldPath, Map<String, Object> extendParams) {
		super(source);
		this.catalog = catalog;
		this.oldPath = oldPath;
		this.extendParams = extendParams;
	}
}
