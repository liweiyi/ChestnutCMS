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
package com.chestnut.customform;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsBookTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsCustomFormTips implements TipMessage {

    /**
     * 正在导出自定义表单数据
     */
    EXPORTING_CUSTOM_FORM,

    /**
     * 正在导入自定义表单
     */
    IMPORTING_CUSTOM_FORM,

    /**
     * "导入自定义表单`{0}`失败：{1}
     */
    IMPORT_CUSTOM_FORM_FAIL,

    /**
     * 正在导入自定义表单元数据模型
     */
    IMPORTING_CUSTOM_FORM_MODEL,

    /**
     * "导入自定义表单元数据模型`{0}`失败：{1}
     */
    IMPORT_CUSTOM_FORM_MODEL_FAIL,

    /**
     * 正在导入自定义表单模型字段
     */
    IMPORTING_CUSTOM_FORM_MODEL_FIELD,

    /**
     * "导入自定义表单模型字段`{0}`失败：{1}
     */
    IMPORT_CUSTOM_FORM_MODEL_FIELD_FAIL,

    /**
     * 正在导入自定义表单数据
     */
    IMPORTING_CUSTOM_FORM_DATA,

    /**
     * "导入自定义及表单数据`{0}`失败：{1}
     */
    IMPORT_CUSTOM_FORM_DATA_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.CUSTOMFORM." + this.name();
    }
}
