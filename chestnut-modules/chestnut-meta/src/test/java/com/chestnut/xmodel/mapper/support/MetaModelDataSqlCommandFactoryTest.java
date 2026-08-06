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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetaModelDataSqlCommandFactoryTest {

	@Test
	void usesCanonicalMetadataIdentifiers() {
		DBTable table = table("CMS_CFD_CUSTOM", "MODEL_ID", "TITLE");
		table.setSchema("CMS_OWNER");
		MetaModelDataSqlCommandFactory factory = new MetaModelDataSqlCommandFactory(new StubDBService(table));

		MetaModelDataSqlCommand command = factory.create("cms_cfd_custom",
				Map.of("title", "value"), Map.of("model_id", 1L));

		assertEquals("\"CMS_OWNER\".\"CMS_CFD_CUSTOM\"", command.table());
		assertEquals("\"TITLE\"", command.values().get(0).column());
		assertEquals("\"MODEL_ID\"", command.conditions().get(0).column());
	}

	@Test
	void rejectsColumnsOutsidePhysicalTableMetadata() {
		DBTable table = table("CMS_CFD_CUSTOM", "MODEL_ID", "TITLE");
		MetaModelDataSqlCommandFactory factory = new MetaModelDataSqlCommandFactory(new StubDBService(table));

		assertThrows(RuntimeException.class, () -> factory.create("CMS_CFD_CUSTOM",
				Map.of("TITLE; DROP TABLE CMS_CFD_CUSTOM", "value"), Map.of("MODEL_ID", 1L)));
	}

	private static DBTable table(String tableName, String... columnNames) {
		DBTable table = new DBTable();
		table.setName(tableName);
		for (String columnName : columnNames) {
			DBTableColumn column = new DBTableColumn();
			column.setName(columnName);
			table.getColumns().add(column);
		}
		return table;
	}

	private static class StubDBService extends DBService {

		private final DBTable table;

		private StubDBService(DBTable table) {
			super(null);
			this.table = table;
		}

		@Override
		public Optional<DBTable> findTable(String tableName) {
			return this.table.getName().equalsIgnoreCase(tableName) ? Optional.of(this.table) : Optional.empty();
		}

		@Override
		public String quoteIdentifier(String identifier) {
			return "\"" + identifier.replace("\"", "\"\"") + "\"";
		}
	}
}
