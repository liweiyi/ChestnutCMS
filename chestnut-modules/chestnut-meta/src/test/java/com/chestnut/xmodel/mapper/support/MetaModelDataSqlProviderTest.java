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

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetaModelDataSqlProviderTest {

	private final MetaModelDataSqlProvider provider = new MetaModelDataSqlProvider();

	@Test
	void generatesBoundCrudSql() {
		MetaModelDataSqlCommand command = new MetaModelDataSqlCommand(
				"`cms_cfd_custom`",
				List.of(
						new MetaModelDataSqlCommand.ColumnValue("`title`", "unsafe ' value"),
						new MetaModelDataSqlCommand.ColumnValue("`remark`", null)
				),
				List.of(new MetaModelDataSqlCommand.ColumnValue("`data_id`", 1L))
		);
		Map<String, Object> params = Map.of("command", command);

		assertEquals("INSERT INTO `cms_cfd_custom` (`title`, `remark`) VALUES "
				+ "(#{command.values[0].value}, #{command.values[1].value})", provider.insert(params));
		assertEquals("UPDATE `cms_cfd_custom` SET `title` = #{command.values[0].value}, "
				+ "`remark` = #{command.values[1].value} WHERE `data_id` = #{command.conditions[0].value}",
				provider.update(params));
		assertEquals("DELETE FROM `cms_cfd_custom` WHERE `data_id` = #{command.conditions[0].value}",
				provider.delete(params));
		assertFalse(provider.insert(params).contains("unsafe ' value"));

		MetaObject metaObject = SystemMetaObject.forObject(params);
		assertEquals("unsafe ' value", metaObject.getValue("command.values[0].value"));
		assertEquals(1L, metaObject.getValue("command.conditions[0].value"));
	}

	@Test
	void rendersNullConditionsWithoutBinding() {
		MetaModelDataSqlCommand command = new MetaModelDataSqlCommand(
				"\"cms_exd_custom\"", List.of(),
				List.of(new MetaModelDataSqlCommand.ColumnValue("\"data_type\"", null)));

		assertEquals("SELECT * FROM \"cms_exd_custom\" WHERE \"data_type\" IS NULL",
				provider.select(Map.of("command", command)));
	}

	@Test
	void rejectsUnboundedWrites() {
		MetaModelDataSqlCommand command = new MetaModelDataSqlCommand("`cms_data`",
				List.of(new MetaModelDataSqlCommand.ColumnValue("`title`", "value")), List.of());

		assertThrows(IllegalArgumentException.class, () -> provider.update(Map.of("command", command)));
		assertThrows(IllegalArgumentException.class, () -> provider.delete(Map.of("command", command)));
	}
}
