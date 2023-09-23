package com.chestnut.customform.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.xmodel.core.BaseModelData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 自定义表单默认数据表 [CmsCustomFormData]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CmsCustomFormData.TABLE_NAME, autoResultMap = true)
public class CmsCustomFormData extends BaseModelData {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_cfd_default";

    @TableId(value = "data_id", type = IdType.INPUT)
    private Long dataId;

    /**
     * 关联表单ID（元数据模型ID）
     */
    private Long modelId;
    
    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * IP
     */
    private String clientIp;

    /**
     * 用户唯一性标识（浏览器指纹，会员ID）
     */
    private String uuid;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
