package com.chestnut.search.domain;

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 索引模型
 */
@Getter
@Setter
@TableName(value = IndexModel.TABLE_NAME)
public class IndexModel extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "search_index_model";

	/*
	 * 模型主键ID
	 */
	@TableId(value = "model_id", type = IdType.INPUT)
    private Long modelId;
	
	/*
	 * 搜索类型
	 */
	private String type;
	
	/*
	 * 模型名称
	 */
	private String name;
	
	/*
	 * 模型唯一标识编码（索引名）
	 */
	private String code;
	
	/*
	 * 模型字段列表
	 */
	@TableField(exist = false)
	private List<IndexModelField> fields;
}
