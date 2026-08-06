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
package com.chestnut.common.db;

import com.chestnut.common.db.domain.DBTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DBServiceTest {

	@Test
	void quotesAndEscapesIdentifiersForSupportedDatabases() {
		assertEquals("`cms_data`", DBService.quoteIdentifier("cms_data", "`"));
		assertEquals("`cms``data`", DBService.quoteIdentifier("cms`data", "`"));
		assertEquals("\"MixedCase\"", DBService.quoteIdentifier("MixedCase", "\""));
		assertEquals("\"a\"\"b\"", DBService.quoteIdentifier("a\"b", "\""));
	}

	@Test
	void rejectsUnsafeUnquotedIdentifiers() {
		assertEquals("cms_data", DBService.quoteIdentifier("cms_data", ""));
		assertThrows(IllegalArgumentException.class,
				() -> DBService.quoteIdentifier("cms data", ""));
		assertThrows(IllegalArgumentException.class,
				() -> DBService.quoteIdentifier("cms_data\nDROP TABLE x", "\""));
	}

	@Test
	void fallsBackToCanonicalTableNameLookup() {
		DBTable table = new DBTable();
		table.setName("CMS_CFD_CUSTOM");
		DBService service = new DBService(null) {
			@Override
			public List<DBTable> listTables(String tableName) {
				return tableName == null ? List.of(table) : List.of();
			}
		};

		assertEquals(table, service.findTable("cms_cfd_custom").orElseThrow());
	}
}
