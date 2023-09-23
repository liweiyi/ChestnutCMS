package com.chestnut.common.db.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DBTable {

	/**
	 * 数据库名
	 */
	private String catalog;

	/**
	 * 类型（TABLE_TYPE）
	 */
	private String type;

	/**
	 * 表名（TABLE_NAME ）
	 */
	private String name;

	/**
	 * 备注（REMARKS）
	 */
	private String comment;

	/**
	 * 表字段
	 */
	private List<DBTableColumn> columns = new ArrayList<>();
}
