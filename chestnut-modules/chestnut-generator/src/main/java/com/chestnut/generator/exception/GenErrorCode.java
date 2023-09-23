package com.chestnut.generator.exception;

import com.chestnut.common.exception.ErrorCode;

public enum GenErrorCode implements ErrorCode {
	
	/**
	 * 渲染模板失败，表名：{0}
	 */
	TEMPLATE_ERR, 
	
	/**
	 * 导入失败：{0}
	 */
	IMPORT_ERR,

	/**
	 * 同步数据失败，原表结构不存在
	 */
	SOURCE_TABLE_NOT_EXISTS,
	
	/**
	 * 树编码字段不能为空
	 */
	TREE_CODE_EMPTY,

	/**
	 * 树名称字段不能为空
	 */
	TREE_NAME_EMPTY,
	
	/**
	 * 树父编码字段不能为空
	 */
	PARENT_TREE_CODE_EMPTY,
	
	/**
	 * 关联子表的表名不能为空
	 */
	SUBTABLE_NAME_EMPTY,
	
	/**
	 * "子表关联的外键名不能为空"
	 */
	SUBTABLE_FK_EMPTY;

	@Override
	public String value() {
		return "{ERRCODE.GEN." + this.name() + "}";
	}

}
