package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 模板表对象 [cms_template]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsTemplate.TABLE_NAME)
public class CmsTemplate extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_template";

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.INPUT)
    private Long templateId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 发布通道编码
     */
    private String publishPipeCode;

    /**
     * 模板文件路径
     */
    private String path;
    
    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板文件大小
     */
    private Long filesize;
    
    /**
     * 模板文件最后变更时间
     */
    private Long modifyTime;
}
