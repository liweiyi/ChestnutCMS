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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetaModelDataSqlProvider {

	public String select(Map<String, Object> params) {
		MetaModelDataSqlCommand command = this.getCommand(params);
		return "SELECT * FROM " + command.table() + this.where(command);
	}

	public String selectCount(Map<String, Object> params) {
		MetaModelDataSqlCommand command = this.getCommand(params);
		return "SELECT COUNT(*) FROM " + command.table() + this.where(command);
	}

	public String insert(Map<String, Object> params) {
		MetaModelDataSqlCommand command = this.getCommand(params);
		this.requireValues(command);
		String columns = command.values().stream()
				.map(MetaModelDataSqlCommand.ColumnValue::column)
				.collect(Collectors.joining(", "));
		String placeholders = IntStream.range(0, command.values().size())
				.mapToObj(i -> "#{command.values[" + i + "].value}")
				.collect(Collectors.joining(", "));
		return "INSERT INTO " + command.table() + " (" + columns + ") VALUES (" + placeholders + ")";
	}

	public String update(Map<String, Object> params) {
		MetaModelDataSqlCommand command = this.getCommand(params);
		this.requireValues(command);
		this.requireConditions(command);
		String assignments = IntStream.range(0, command.values().size())
				.mapToObj(i -> command.values().get(i).column()
						+ " = #{command.values[" + i + "].value}")
				.collect(Collectors.joining(", "));
		return "UPDATE " + command.table() + " SET " + assignments + this.where(command);
	}

	public String delete(Map<String, Object> params) {
		MetaModelDataSqlCommand command = this.getCommand(params);
		this.requireConditions(command);
		return "DELETE FROM " + command.table() + this.where(command);
	}

	private String where(MetaModelDataSqlCommand command) {
		if (command.conditions().isEmpty()) {
			return "";
		}
		String conditions = IntStream.range(0, command.conditions().size())
				.mapToObj(i -> {
					MetaModelDataSqlCommand.ColumnValue condition = command.conditions().get(i);
					if (condition.value() == null) {
						return condition.column() + " IS NULL";
					}
					return condition.column() + " = #{command.conditions[" + i + "].value}";
				})
				.collect(Collectors.joining(" AND "));
		return " WHERE " + conditions;
	}

	private MetaModelDataSqlCommand getCommand(Map<String, Object> params) {
		Object command = params.get("command");
		if (command instanceof MetaModelDataSqlCommand sqlCommand) {
			return sqlCommand;
		}
		throw new IllegalArgumentException("Meta model data SQL command is required.");
	}

	private void requireValues(MetaModelDataSqlCommand command) {
		if (command.values().isEmpty()) {
			throw new IllegalArgumentException("Meta model data SQL values cannot be empty.");
		}
	}

	private void requireConditions(MetaModelDataSqlCommand command) {
		if (command.conditions().isEmpty()) {
			throw new IllegalArgumentException("Meta model data update/delete conditions cannot be empty.");
		}
	}
}
