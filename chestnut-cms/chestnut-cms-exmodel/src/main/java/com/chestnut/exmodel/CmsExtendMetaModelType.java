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
package com.chestnut.exmodel;

import com.chestnut.exmodel.domain.CmsExtendModelData;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.core.impl.MetaControlType_Input;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 元数据模型类型：扩展模型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaModelType.BEAN_PREFIX + CmsExtendMetaModelType.TYPE)
public class CmsExtendMetaModelType implements IMetaModelType {

    public final static String TYPE = "CmsExtend";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTableNamePrefix() {
        return "cms_exd_";
    }
    @Override
    public String getDefaultTable() {
        return CmsExtendModelData.TABLE_NAME;
    }

    @Override
    public List<MetaModelField> getFixedFields() {
        return List.of(FIELD_MODEL_ID, FIELD_DATA_ID, FIELD_DATA_TYPE);
    }

    /**
     * 数据表必须包含的字段
     */
    public static final MetaModelField FIELD_MODEL_ID = new MetaModelField("模型ID", "modelId",
            MODEL_ID_FIELD_NAME, true, MetaControlType_Input.TYPE);

    public static final MetaModelField FIELD_DATA_ID = new MetaModelField("数据ID", "dataId",
            "data_id", true, MetaControlType_Input.TYPE);

    public static final MetaModelField FIELD_DATA_TYPE = new MetaModelField("数据类型", "dataType",
            "data_type", true, MetaControlType_Input.TYPE);
}
