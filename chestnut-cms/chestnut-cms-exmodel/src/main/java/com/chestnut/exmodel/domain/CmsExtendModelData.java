package com.chestnut.exmodel.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.xmodel.core.BaseModelData;
import lombok.Getter;
import lombok.Setter;

/**
 * 扩展模型数据默认表 [ExtendModelData]
 */
@Getter
@Setter
@TableName(CmsExtendModelData.TABLE_NAME)
public class CmsExtendModelData extends BaseModelData {
    
    public static final String TABLE_NAME = "cms_exd_default";

    /**
     * 关联数据ID（联合主键）
     */
    private Long dataId;

    /**
     * 关联数据类型（联合主键）
     */
    private String dataType;

    /**
     * 关联元数据模型ID（联合主键）
     */
    private Long modelId;

    @Override
    public void setFieldValue(String fieldName, Object fieldValue) {
        switch(fieldName) {
            case "data_id" -> this.setDataId(ConvertUtils.toLong(fieldValue));
            case "data_type" -> this.setDataType(ConvertUtils.toStr(fieldValue));
            case "model_id" -> this.setModelId(ConvertUtils.toLong(fieldValue));
            default -> super.setFieldValue(fieldName, fieldValue);
        }
    }
}
