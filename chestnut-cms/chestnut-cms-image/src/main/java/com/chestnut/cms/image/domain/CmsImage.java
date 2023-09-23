package com.chestnut.cms.image.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntityWithLogicDelete;
import lombok.Getter;
import lombok.Setter;

/**
 * 图集图片表对象 [cms_image]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsImage.TABLE_NAME)
public class CmsImage extends BaseEntityWithLogicDelete {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_image";

    @TableId(value = "image_id", type = IdType.INPUT)
    private Long imageId;

    /**
     * 所属内容ID
     */
    private Long contentId;
    
    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 图片标题
     */
    private String title;

    /**
     * 图片摘要
     */
    private String description;

    /**
     * 图片原文件名
     */
    private String fileName;

    /**
     * 图片路径
     */
    private String path;

    /**
     * 图片路径
     */
    @TableField(exist = false)
    private String src;

    /**
     * 图片类型
     */
    private String imageType;
    
    /**
     * 图片大小
     */
    private Long fileSize;
    
    @TableField(exist = false)
    private String fileSizeName;

    /**
     * 图片宽
     */
    private Integer width;

    /**
     * 图片高
     */
    private Integer height;

    /**
     * 跳转链接
     */
    private String redirectUrl;

    /**
     * 图片点击量
     */
    private Integer hitCount;

    /**
     * 排序字段
     */
    private Integer sortFlag;
}
