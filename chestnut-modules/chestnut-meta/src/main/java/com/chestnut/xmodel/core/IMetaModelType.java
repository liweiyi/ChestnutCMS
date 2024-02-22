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
package com.chestnut.xmodel.core;

import java.util.List;

/**
 * 元数据模型类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMetaModelType {

    String BEAN_PREFIX = "MetaModelType_";

    /**
     * 扩展模型字段名前缀（避免与源数据字段冲突）
     */
    String DATA_FIELD_PREFIX = "ModelData_";

    /**
     * 数据表必须包含的字段
     */
    String MODEL_ID_FIELD_NAME = "model_id";

    /**
     * 类型唯一标识
     */
    String getType();

    /**
     * 可用数据表名称固定前缀
     */
    String getTableNamePrefix();

    /**
     * 是否默认表
     */
    String getDefaultTable();

    /**
     * 模型数据表固定字段
     */
    List<MetaModelField> getFixedFields();
}
