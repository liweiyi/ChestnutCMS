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
package com.chestnut.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface DataBaseMapper {

	/**
	 * 表字段是否存在
	 * 
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	@Select("select count(*) from information_schema.columns where table_name = #{tableName} and column_name = #{columnName}")
	public Integer isTableColumnExists(@Param("tableName") String tableName, @Param("columnName") String columnName);

	/**
	 * 添加表字段
	 * 
	 * @param tableName  表明
	 * @param columnName 字段名
	 * @param columnType 字段类型
	 */
	@Update("alter table #{tableName} add column #{columnName} #{columnType}")
	public void addTableColumn(@Param("tableName") String tableName, @Param("columnName") String columnName,
			@Param("columnType") String columnType);

	/**
	 * 重命名表
	 * 
	 * @param oldName
	 * @param newName
	 */
	@Update("alter table #{oldTableName} rename #{newTableName}")
	public void renameTable(String oldTableName, String newTableName);

	/**
	 * 清空表数据，自增清零
	 * 
	 * @param tableName
	 */
	@Update("truncate table ${tableName}")
	public void truncateTable(@Param("tableName") String tableName);
	
	/**
	 * 当前数据库名
	 */
	@Select("select database()")
	public void getDatabaseName();
}
