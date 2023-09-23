package com.chestnut.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签对象 [cms_tag_word]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(TagWord.TABLE_NAME)
public class TagWord extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cc_tag_word";

    @TableId(value = "word_id", type = IdType.INPUT)
    private Long wordId;

    /**
     * 所属分组ID
     */
    private Long groupId;

    /**
     * 名称
     */
    private String word;

    /**
     * 图片路径
     */
    private String logo;

    /**
     * 使用次数
     */
    private String useCount;

    /**
     * 点击次数
     */
    private String hitCount;

    /**
     * 图片预览路径
     */
    @TableField(exist = false)
    private String src;
    
    /**
     * 排序标识
     */
    private Long sortFlag;
}
