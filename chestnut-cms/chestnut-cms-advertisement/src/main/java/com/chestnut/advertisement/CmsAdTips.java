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
package com.chestnut.advertisement;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsAdTips implements TipMessage {

    /** 正在删除广告数据：{0}/{1} */
    DELETING_ADVERTISEMENT_DATA,
    /** 删除广告数据失败，请查看系统日志 */
    DELETE_ADVERTISEMENT_FAILED,
    /** 正在统计广告资源引用：{0}/{1} */
    STAT_RESOURCE_REFERENCES,
    /** 广告无效或不可访问 */
    INVALID_ADVERTISEMENT,

    /**
     * 正在导出广告数据
     */
    EXPORTING_AD,

    /**
     * 正在导入广告数据
     */
    IMPORTING_AD,

    /**
     * 导入广告数据`{0}`失败：{1}
     */
    IMPORT_AD_FAIL;

    @Override
    public String value() {
        return "TIP.CMS.AD." + this.name();
    }
}
