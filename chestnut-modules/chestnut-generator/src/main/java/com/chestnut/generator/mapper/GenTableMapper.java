package com.chestnut.generator.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.generator.domain.GenTable;

/**
 * 业务 数据层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface GenTableMapper extends BaseMapper<GenTable> {

	/**
	 * 查询据库列表
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 数据库表集合
	 */
	@Select({ "<script>" 
			+ "select table_name, table_comment, create_time, update_time from information_schema.tables"
			+ " where table_schema = (select database())"
			+ " AND table_name NOT LIKE 'qrtz_%' AND table_name NOT LIKE 'gen_%'"
			+ " AND table_name NOT IN (select table_name from gen_table)"
            + "<if test='tableName!=null'>"  
	    	+ " AND table_name like '%#{tableName}%'"
            + "</if>"  
            + "<if test='tableComment!=null'>"  
	    	+ " AND table_comment like '%#{tableComment}%'"
            + "</if>"
            + " order by create_time desc"
			+ "</script>" })
	public List<GenTable> selectDbTableList(@Param("tableName") String tableName, @Param("tableComment") String tableComment);

	/**
	 * 查询据库列表
	 * 
	 * @param tableNames
	 *            表名称组
	 * @return 数据库表集合
	 */
	public List<GenTable> selectDbTableListByNames(String[] tableNames);

	/**
	 * 查询所有表信息
	 * 
	 * @return 表信息集合
	 */
	public List<GenTable> selectGenTableAll();

	/**
	 * 查询表ID业务信息
	 * 
	 * @param id
	 *            业务ID
	 * @return 业务信息
	 */
	public GenTable selectGenTableById(Long id);

	/**
	 * 查询表名称业务信息
	 * 
	 * @param tableName
	 *            表名称
	 * @return 业务信息
	 */
	public GenTable selectGenTableByName(String tableName);

	/**
	 * 新增业务
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 结果
	 */
	public int insertGenTable(GenTable genTable);

	/**
	 * 修改业务
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 结果
	 */
	public int updateGenTable(GenTable genTable);

	/**
	 * 批量删除业务
	 * 
	 * @param ids
	 *            需要删除的数据ID
	 * @return 结果
	 */
	public int deleteGenTableByIds(Long[] ids);
}
