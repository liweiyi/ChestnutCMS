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
package com.chestnut.cms.image.utils;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsImageContentTips implements TipMessage {

    /** 正在删除图片内容数据：{0}/{1} */
    DELETING_IMAGE_DATA,
    /** 正在删除图片内容备份数据：{0}/{1} */
    DELETING_IMAGE_BACKUP_DATA,
    /** 删除图片内容数据失败，请查看系统日志 */
    DELETE_IMAGE_FAILED,
    /** 删除图片内容备份数据失败，请查看系统日志 */
    DELETE_IMAGE_BACKUP_FAILED,
    /** 正在统计图集内容资源引用：{0}/{1} */
    STAT_RESOURCE_REFERENCES,

    /**
     * 正在导出图片内容详情数据
     */
    EXPORTING_IMAGE_CONTENT,

    /**
     * 正在导入图片内容详情数据
     */
    IMPORTING_IMAGE_CONTENT,

    /**
     * 导入图片内容数据`{0}`失败：{1}
     */
    IMPORT_IMAGE_CONTENT_FAIL;

    @Override
    public String value() {
        return "TIP.CMS.IMG." + this.name();
    }
}
