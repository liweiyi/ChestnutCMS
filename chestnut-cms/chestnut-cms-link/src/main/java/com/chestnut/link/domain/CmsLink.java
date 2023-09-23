package com.chestnut.link.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 友情链接表对象 [cms_link]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsLink.TABLE_NAME)
public class CmsLink extends BaseEntity {

    private static final long serialVersionUID=1L;

	public static final String TABLE_NAME = "cms_link";

    @TableId(value = "link_id", type = IdType.INPUT)
    private Long linkId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 所属分组ID
     */
    private Long groupId;

    /**
     * 链接名称
     */
    private String name;

    /**
     * 链接URL
     */
    private String url;

    /**
     * 图片路径
     */
    private String logo;

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
