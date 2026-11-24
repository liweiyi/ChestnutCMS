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
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsLinkTips implements TipMessage {

    /** 正在删除友情链接数据：{0}/{1} */
    DELETING_FRIEND_LINK_DATA,
    /** 正在删除友链分组数据：{0}/{1} */
    DELETING_FRIEND_LINK_GROUP_DATA,
    /** 删除友情链接数据失败，请查看系统日志 */
    DELETE_FRIEND_LINK_FAILED,
    /** 正在统计友情链接资源引用：{0}/{1} */
    STAT_RESOURCE_REFERENCES,
    /** 友链分组编码重复 */
    GROUP_CODE_CONFLICT,

    /**
     * 正在导出友情链接数据
     */
    EXPORTING_FLINK,

    /**
     * 正在导入友情链接分组数据
     */
    IMPORTING_FLINK_GROUP,

    /**
     * 友情链接分组 {0} 导入失败：{1}
     */
    IMPORT_FLINK_GROUP_FAIL,

    /**
     * 正在导入友情链接数据
     */
    IMPORTING_FLINK,

    /**
     * 友情链接 {0} 导入失败：{1}
     */
    IMPORT_FLINK_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.FLINK." + this.name();
    }
}
