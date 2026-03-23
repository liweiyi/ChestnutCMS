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
package com.chestnut.link;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsLinkTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsLinkTips implements TipMessage {

    /**
     * 正在导出友情链接数据
     */
    EXPORTING_FLINK,

    /**
     * 正在导入友情链接分组数据
     */
    IMPORTING_FLINK_GROUP,

    /**
     * 友情链接分组导入失败
     */
    IMPORT_FLINK_GROUP_FAIL,

    /**
     * 正在导入友情链接数据
     */
    IMPORTING_FLINK,

    /**
     * 友情链接导入失败
     */
    IMPORT_FLINK_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.FLINK." + this.name();
    }
}
