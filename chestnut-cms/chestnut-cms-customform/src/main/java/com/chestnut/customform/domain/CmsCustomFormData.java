package com.chestnut.customform.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.utils.ConvertUtils;
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

    @Override
    public void setFieldValue(String fieldName, Object fieldValue) {
        switch(fieldName) {
            case "data_id" -> this.setDataId(ConvertUtils.toLong(fieldValue));
            case "model_id" -> this.setModelId(ConvertUtils.toLong(fieldValue));
            case "site_id" -> this.setSiteId(ConvertUtils.toLong(fieldValue));
            case "client_ip" -> this.setClientIp(ConvertUtils.toStr(fieldValue));
            case "uuid" -> this.setUuid(ConvertUtils.toStr(fieldValue));
            case "create_time" -> this.setCreateTime(ConvertUtils.toLocalDateTime(fieldValue));
            default -> super.setFieldValue(fieldName, fieldValue);
        }
    }
}
