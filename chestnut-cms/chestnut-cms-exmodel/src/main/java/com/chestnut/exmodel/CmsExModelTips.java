/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.chestnut.common.exception.TipMessage;

/**
 * CmsDynamicTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsExModelTips implements TipMessage {

    /**
     * 正在导出扩展模型数据
     */
    EXPORTING_EXMODEL,

    /**
     * 正在导入扩展模型
     */
    IMPORTING_EXMODEL,

    /**
     * 导入导入扩展模型`{0}`失败：{1}"
     */
    IMPORT_EXMODEL_FAIL,

    /**
     * 正在导入扩展模型字段
     */
    IMPORTING_EXMODEL_FIELD,

    /**
     * 导入扩展模型字段`{0}`失败：{1}"
     */
    IMPORT_EXMODEL_FIELD_FAIL,

    /**
     * 正在导入扩展模型数据
     */
    IMPORTING_EXMODEL_DATA,

    /**
     * 导入扩展模型数据`{0}`失败：{1}"
     */
    IMPORT_EXMODEL_DATA_FAIL,

    /**
     * 更新站点及栏目扩展模型配置失败
     */
    UPDATE_EXMODEL_CONFIG_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.EXMODEL." + this.name();
    }
}
