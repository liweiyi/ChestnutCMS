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

import com.chestnut.common.db.DBService;
import com.chestnut.common.db.domain.DBTable;
import com.chestnut.common.db.domain.DBTableColumn;
import com.chestnut.xmodel.exception.MetaErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MetaModelDataSqlCommandFactory {

	private final DBService dbService;

	public MetaModelDataSqlCommand create(String tableName, Map<String, Object> values,
										  Map<String, Object> conditions) {
		DBTable table = this.dbService.findTable(tableName)
				.orElseThrow(() -> MetaErrorCode.META_TABLE_NOT_EXISTS.exception(tableName));
		return new MetaModelDataSqlCommand(
				this.quoteTable(table),
				this.quoteColumns(table, values),
				this.quoteColumns(table, conditions)
		);
	}

	private String quoteTable(DBTable table) {
		String quotedTable = this.dbService.quoteIdentifier(table.getName());
		if (table.getSchema() == null || table.getSchema().isBlank()) {
			return quotedTable;
		}
		return this.dbService.quoteIdentifier(table.getSchema()) + "." + quotedTable;
	}

	private List<MetaModelDataSqlCommand.ColumnValue> quoteColumns(DBTable table, Map<String, Object> values) {
		return values.entrySet().stream()
				.map(entry -> new MetaModelDataSqlCommand.ColumnValue(
						this.dbService.quoteIdentifier(this.resolveColumn(table, entry.getKey()).getName()),
						entry.getValue()))
				.toList();
	}

	private DBTableColumn resolveColumn(DBTable table, String columnName) {
		return table.getColumns().stream()
				.filter(column -> column.getName().equals(columnName))
				.findFirst()
				.orElseGet(() -> {
					List<DBTableColumn> matches = table.getColumns().stream()
							.filter(column -> column.getName().equalsIgnoreCase(columnName))
							.toList();
					if (matches.size() != 1) {
						throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(columnName);
					}
					return matches.get(0);
				});
	}
}
