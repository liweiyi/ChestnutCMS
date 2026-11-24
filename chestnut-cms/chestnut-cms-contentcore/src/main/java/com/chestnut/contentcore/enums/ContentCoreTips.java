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

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum ContentCoreTips implements TipMessage {

    /** 正在删除内容数据：{0}/{1} */
    DELETING_CONTENT_DATA,
    /** 正在删除内容备份数据：{0}/{1} */
    DELETING_CONTENT_BACKUP_DATA,
    /** 正在删除资源数据：{0}/{1} */
    DELETING_RESOURCE_DATA,
    /** 正在删除栏目数据：{0}/{1} */
    DELETING_CATALOG_DATA,
    /** 正在删除站点扩展属性数据：{0}/{1} */
    DELETING_SITE_PROPERTY_DATA,
    /** 正在删除模板数据：{0}/{1} */
    DELETING_TEMPLATE_DATA,
    /** 正在删除内容关联数据：{0}/{1} */
    DELETING_CONTENT_RELATION_DATA,
    /** 删除内容数据失败，请查看系统日志 */
    DELETE_CONTENT_FAILED,
    /** 删除资源数据失败，请查看系统日志 */
    DELETE_RESOURCE_FAILED,
    /** 删除栏目数据失败，请查看系统日志 */
    DELETE_CATALOG_FAILED,
    /** 删除站点扩展属性数据失败，请查看系统日志 */
    DELETE_SITE_PROPERTY_FAILED,
    /** 删除模板数据失败，请查看系统日志 */
    DELETE_TEMPLATE_FAILED,
    /** 删除内容关联数据失败，请查看系统日志 */
    DELETE_CONTENT_RELATION_FAILED,
    /** 正在统计内容LOGO资源引用：{0}/{1} */
    STAT_CONTENT_LOGO_REFERENCES,
    /** 正在统计栏目LOGO资源引用：{0}/{1} */
    STAT_CATALOG_LOGO_REFERENCES,
    /** 操作失败：{0} */
    TASK_FAILED,
    /** 正在删除页面部件数据 */
    DELETING_PAGE_WIDGETS,
    /** 正在合并内容：{0} [{1}/{2}] */
    MERGING_CONTENT,
    /** 更新转移栏目原父级栏目数据 */
    UPDATING_SOURCE_PARENT,
    /** 更新转移栏目数据 */
    UPDATING_MOVED_CATALOG,
    /** 更新转移栏目子栏目数据 */
    UPDATING_CHILD_CATALOGS,
    /** 更新转移目标栏目数据 */
    UPDATING_TARGET_CATALOG,
    /** 更新转移栏目及其子栏目内容 */
    UPDATING_MOVED_CONTENTS,
    /** 下载远程图片失败：{0} */
    REMOTE_IMAGE_FAILED,
    /** 排序数值不能为0 */
    SORT_ZERO,
    /** 属性代码不能重复 */
    PROPERTY_CODE_CONFLICT,
    /** 模板未找到：{0} */
    TEMPLATE_MISSING,
    /** 页面部件未找到：{0} */
    PAGE_WIDGET_NOT_FOUND,
    /** 操作超时 */
    OPERATION_TIMEOUT,
    /** 参数与内部链接固定参数 id 冲突 */
    INTERNAL_URL_PARAM_CONFLICT,

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
     * 正在删除内容数据：{0}
     */
    DELETING_CONTENT,

    /**
     * 正在删除页面部件数据：{0}
     */
    DELETING_PAGE_WIDGET,

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

    /**
     * 来源数据不存在
     */
    MISSING_COPY_SOURCE,

    /**
     * 内容未找到：{0}
     */
    CONTENT_NOT_FOUND_BY_ID,
    ;

    @Override
    public String value() {
        return "TIP.CMS.CORE." + this.name();
    }
}
