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
package com.chestnut.block;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.chestnut.block.ManualPageWidgetType.RowData;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.AbstractPageWidget;
import com.chestnut.contentcore.domain.CmsPageWidget;

/**
 * 手动控制区块
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ManualPageWidget extends AbstractPageWidget {
	
	@Override
	public void add() {
		this.dealContentIgnoreFields();
		super.add();
	}
	
	@Override
	public void save() {
		this.dealContentIgnoreFields();
		super.save();
	}
	
	private void dealContentIgnoreFields() {
		CmsPageWidget pageWidgetEntity = this.getPageWidgetEntity();
		List<RowData> rows = JacksonUtils.fromList(pageWidgetEntity.getContent(), RowData.class);
		if (Objects.nonNull(rows)) {
			rows.forEach(row -> {
				row.getItems().forEach(item -> item.setLogoSrc(null));
			});
		} else {
			rows = Collections.emptyList();
		}
		pageWidgetEntity.setContent(JacksonUtils.to(rows));
	}
}
