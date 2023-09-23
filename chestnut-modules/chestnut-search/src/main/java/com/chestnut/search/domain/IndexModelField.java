package com.chestnut.search.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 索引模型字段表
 */
@Getter
@Setter
@TableName(value = IndexModelField.TABLE_NAME)
public class IndexModelField extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "search_index_model_field";

	/*
	 * 模型字段ID
	 */
	@TableId(value = "field_id", type = IdType.INPUT)
    private Long fieldId;

	/*
	 * 所属模型ID 
	 */
    private Long modelId;
	
	/*
	 * 字段标签
	 */
	private String fieldLabel;
	
	/*
	 * 字段名
	 */
	private String fieldName;
	
	/*
	 * 字段类型
	 */
	private String fieldType;
	
	/*
	 * 是否主键
	 */
	private String primaryKey;
	
	/*
	 * 字段权重
	 */
	private Double weight;
	
	/*
	 * 是否分词
	 */
	private String index;
	
	/*
	 * 分词策略
	 */
	private String analyzer;
}
