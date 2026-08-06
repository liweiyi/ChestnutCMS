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
package com.chestnut.xmodel.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chestnut.xmodel.mapper.support.MetaModelDataSqlCommand;
import com.chestnut.xmodel.mapper.support.MetaModelDataSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface MetaModelDataMapper {

	@SelectProvider(type = MetaModelDataSqlProvider.class, method = "selectCount")
	long selectCount(@Param("command") MetaModelDataSqlCommand command);

	@SelectProvider(type = MetaModelDataSqlProvider.class, method = "select")
	Map<String, Object> selectOne(@Param("command") MetaModelDataSqlCommand command);

	@SelectProvider(type = MetaModelDataSqlProvider.class, method = "select")
	List<Map<String, Object>> selectList(@Param("command") MetaModelDataSqlCommand command);

	@SelectProvider(type = MetaModelDataSqlProvider.class, method = "select")
	IPage<Map<String, Object>> selectPage(IPage<Map<String, Object>> page,
										 @Param("command") MetaModelDataSqlCommand command);

	@InsertProvider(type = MetaModelDataSqlProvider.class, method = "insert")
	int insert(@Param("command") MetaModelDataSqlCommand command);

	@UpdateProvider(type = MetaModelDataSqlProvider.class, method = "update")
	int update(@Param("command") MetaModelDataSqlCommand command);

	@DeleteProvider(type = MetaModelDataSqlProvider.class, method = "delete")
	int delete(@Param("command") MetaModelDataSqlCommand command);
}
