package com.chestnut.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 敏感词表对象 [cms_sensitive_word]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SensitiveWord.TABLE_NAME)
public class SensitiveWord extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cc_sensitive_word";

    @TableId(value = "word_id", type = IdType.INPUT)
    private Long wordId;

    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    private String word;

    /**
     * 替换词
     */
    private String replaceWord;
}
