/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.exmodel.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.xmodel.core.BaseModelData;
import lombok.Getter;
import lombok.Setter;

/**
 * 扩展模型数据默认表 [ExtendModelData]
 *
 * TODO 2.x版本给扩展模型数据表添加唯一主键ID
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
