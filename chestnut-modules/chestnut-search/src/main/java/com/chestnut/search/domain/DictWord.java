package com.chestnut.search.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 自定义词词库
 */
@Getter
@Setter
@TableName(value = DictWord.TABLE_NAME)
public class DictWord extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "search_dict_word";

	@TableId(value = "word_id", type = IdType.INPUT)
    private Long wordId;
	
	/**
	 * 自定义词类型
	 */
	private String wordType;
	
	/**
	 * 自定义词
	 */
	private String word;
}
