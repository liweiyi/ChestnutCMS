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
