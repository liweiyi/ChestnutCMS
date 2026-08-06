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
package com.chestnut.xmodel.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 元数据模型数据查询条件。
 *
 * <p>上层使用模型字段编码构造条件，物理字段名由元数据服务解析，避免暴露 SQL 构造能力。</p>
 */
public class MetaModelDataQuery {

	private final List<Condition> conditions = new ArrayList<>();

	public MetaModelDataQuery eq(String fieldCode, Object value) {
		this.conditions.add(new Condition(fieldCode, value));
		return this;
	}

	public List<Condition> getConditions() {
		return Collections.unmodifiableList(this.conditions);
	}

	public record Condition(String fieldCode, Object value) {
	}
}
