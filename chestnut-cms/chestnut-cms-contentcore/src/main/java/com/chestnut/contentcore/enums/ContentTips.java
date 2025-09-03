/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import com.chestnut.common.i18n.I18nUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

public enum ContentTips {

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
     * 正在发布栏目：{0}
     */
    PUBLISHING_CATALOG,

    /**
     * 正在发布站点：{0}
     */
    PUBLISHING_SITE,

    /**
     * 发布成功
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
    MOVE_CONTENT_SUCCESS;

    public String locale(Object... args) {
        return locale(LocaleContextHolder.getLocale(), args);
    }

    public String locale(Locale locale, Object... args) {
        if (Objects.isNull(locale)) {
            locale = LocaleContextHolder.getLocale();
        }
        return I18nUtils.parse("TIP.CMS.CORE." + this.name(), locale, args);
    }
}
