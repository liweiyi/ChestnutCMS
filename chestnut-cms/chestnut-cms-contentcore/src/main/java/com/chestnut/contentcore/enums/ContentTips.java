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
package com.chestnut.contentcore.enums;

import com.chestnut.common.exception.TipMessage;

public enum ContentTips implements TipMessage {

    /**
     * 正在删除关联映射内容：{0}，[ID:{1}]
     */
    DELETING_MAPPING_CONTENT,

    /**
     * 正在下线内容：{0}
     */
    OFFLINE_CONTENT,

    /**
     * 正在下线关联映射内容：{0}，[ID:{1}]
     */
    OFFLINE_MAPPING_CONTENT,

    /**
     * 下线成功
     */
    OFFLINE_SUCCESS,

    /**
     * 正在发布内容：{0}
     */
    PUBLISHING_CONTENT,

    /**
     * 正在发布内容：{title} [ {count} / {total} ]
     */
    BATCH_PUBLISHING_CONTENT,

    /**
     * 正在发布栏目：{0}
     */
    PUBLISHING_CATALOG,

    /**
     * 正在发布站点首页：{0}
     */
    PUBLISHING_SITE,

    /**
     * 发布内容成功
     */
    PUBLISH_SUCCESS,

    /**
     * 正在复制：{0} > {1}
     */
    COPYING_CONTENT,

    /**
     * 复制成功
     */
    COPY_CONTENT_SUCCESS,

    /**
     * 正在移动：{0} > {1}
     */
    MOVING_CONTENT,

    /**
     * 移动成功
     */
    MOVE_CONTENT_SUCCESS,

    /**
     * 正在删除站点
     */
    DELETING_SITE,

    /**
     * 正在删除栏目
     */
    DELETING_CATALOG,

    /**
     * 删除成功
     */
    DELETE_SUCCESS,

    /**
     * 保存内容成功
     */
    SAVE_SUCCESS,

    /**
     * 正在删除内容：{0}
     */
    DELETING_CONTENT,

    /**
     * 删除内容成功
     */
    DELETE_CONTENTS_SUCCESS,

    /**
     * [{0}]模板未设置或不存在：{1}
     */
    TEMPLATE_NOT_FOUND,

    /**
     * [{0}]模板解析失败：{1}
     */
    TEMPLATE_PARSE_FAILED,

    /**
     * 正在解压
     */
    EXTRACTING,

    /**
     * 正在导入站点扩展属性数据
     */
    IMPORTING_SITE_PROPS,

    /**
     * 导入站点扩展属性数据`{0}`失败：{1}
     */
    IMPORT_SITE_PROPS_FAIL,

    /**
     * 正在导入资源数据
     */
    IMPORTING_SITE_RESOURCE,

    /**
     * 导入素材资源数据`{0}`失败：{1}
     */
    IMPORT_SITE_RESOURCE_FAIL,

    /**
     * 正在导入栏目数据
     */
    IMPORTING_CATALOG,

    /**
     * 导入栏目数据`{0}`失败：{1}
     */
    IMPORT_CATALOG_FAIL,

    /**
     * 正在导入发布通道数据
     */
    IMPORTING_PUBLISH_PIPE,

    /**
     * 导入发布通道数据`{0}`失败：{1}
     */
    IMPORT_PUBLISH_PIPE_FAIL,

    /**
     * 正在导入页面部件数据
     */
    IMPORTING_PAGEWIDGET,

    /**
     * 导入页面部件关联栏目失败：{0}
     */
    IMPORT_PAGEWIDGET_FAIL_WITH_CATALOG,

    /**
     * 导入页面部件数据`{0}`失败：{1}
     */
    IMPORT_PAGEWIDGET_FAIL,

    /**
     * 正在导入内容数据
     */
    IMPORTING_CONTENT,

    /**
     * 导入内容数据`{0}`失败：{1}
     */
    IMPORT_CONTENT_FAIL,

    /**
     * 正在导入关联内容数据
     */
    IMPORTING_RELATED_CONTENT,

    /**
     * 导入关联内容数据`{0}`失败：{1}
     */
    IMPORT_RELATED_CONTENT_FAIL,

    /**
     * 导入完成
     */
    IMPORT_COMPLETED,

    /**
     * 正在导出站点数据
     */
    EXPORTING_SITE,

    /**
     * 正在导出站点扩展属性数据
     */
    EXPORTING_SITE_PROPS,

    /**
     * 正在导出栏目数据
     */
    EXPORTING_CATALOG,

    /**
     * 正在导出页面部件数据
     */
    EXPORTING_PAGEWIDGET,

    /**
     * 正在导出内容数据
     */
    EXPORTING_CONTENT,

    /**
     * 正在导出关联内容数据
     */
    EXPORTING_RELATED_CONTENT,

    /**
     * 正在导出素材数据
     */
    EXPORTING_RESOURCE,

    /**
     * 正在导出发布通道数据
     */
    EXPORTING_PUBLISH_PIPE,

    /**
     * 导出完成
     */
    EXPORT_COMPLETED,
    ;

    @Override
    public String value() {
        return "TIP.CMS.CORE." + this.name();
    }
}
