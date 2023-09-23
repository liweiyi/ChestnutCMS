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
