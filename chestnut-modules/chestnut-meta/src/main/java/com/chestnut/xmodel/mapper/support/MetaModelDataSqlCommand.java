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
package com.chestnut.xmodel.mapper.support;

import java.util.List;

/**
 * 自定义物理表 SQL 命令。
 *
 * <p>表名、字段名均已使用当前数据库标识符引用符处理，值由 MyBatis 参数绑定。</p>
 */
public record MetaModelDataSqlCommand(String table, List<ColumnValue> values,
										  List<ColumnValue> conditions) {

	public MetaModelDataSqlCommand {
		values = List.copyOf(values);
		conditions = List.copyOf(conditions);
	}

	public record ColumnValue(String column, Object value) {
	}
}
