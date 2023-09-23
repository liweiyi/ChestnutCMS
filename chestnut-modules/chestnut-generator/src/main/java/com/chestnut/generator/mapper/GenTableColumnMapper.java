package com.chestnut.generator.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.generator.domain.GenTableColumn;

/**
 * 业务字段 数据层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {
	
	/**
	 * 根据表名称查询列信息
	 * 
	 * @param tableName
	 *            表名称
	 * @return 列信息
	 */
	public List<GenTableColumn> selectDbTableColumnsByName(String tableName);

	/**
	 * 查询业务字段列表
	 * 
	 * @param tableId
	 *            业务字段编号
	 * @return 业务字段集合
	 */
	public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

	/**
	 * 新增业务字段
	 * 
	 * @param genTableColumn
	 *            业务字段信息
	 * @return 结果
	 */
	public int insertGenTableColumn(GenTableColumn genTableColumn);

	/**
	 * 修改业务字段
	 * 
	 * @param genTableColumn
	 *            业务字段信息
	 * @return 结果
	 */
	public int updateGenTableColumn(GenTableColumn genTableColumn);
}
