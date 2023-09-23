package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 站点自定义属性表对象 [cms_site_property]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsSiteProperty.TABLE_NAME)
public class CmsSiteProperty extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_site_property";

    /**
     * 属性ID-主键
     */
    @TableId(value = "property_id", type = IdType.INPUT)
    private Long propertyId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 属性名称
     */
    @NotBlank
    private String propName;

    /**
     * 属性代码
     */
    @Pattern(regexp = "[A-Za-z0-9_]+")
    private String propCode;

    /**
     * 属性值
     */
    private String propValue;
}
