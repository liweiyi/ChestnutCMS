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
package com.chestnut.cms.word;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsWordTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsWordTips implements TipMessage {

    /**
     * 正在导出TAG词分组数据
     */
    EXPORTING_TAG_GROUP,

    /**
     * 正在导入TAG词分组数据
     */
    IMPORTING_TAG_GROUP,

    /**
     * 导入TAG词分组数据失败
     */
    IMPORT_TAG_GROUP_FAIL,

    /**
     * 正在导出TAG词数据
     */
    EXPORTING_TAG,

    /**
     * 正在导入TAG词数据
     */
    IMPORTING_TAG,

    /**
     * 导入TAG词数据失败
     */
    IMPORT_TAG_FAIL,

    /**
     * 正在导出热词分组数据
     */
    EXPORTING_HOT_WORD_GROUP,

    /**
     * 正在导入热词分组数据
     */
    IMPORTING_HOT_WORD_GROUP,

    /**
     * 导入热词分组数据失败
     */
    IMPORT_HOT_WORD_GROUP_FAIL,

    /**
     * 正在导出热词数据
     */
    EXPORTING_HOT_WORD,

    /**
     * 正在导入热词数据
     */
    IMPORTING_HOT_WORD,

    /**
     * 导入热词数据失败
     */
    IMPORT_HOT_WORD_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.WORD." + this.name();
    }
}
