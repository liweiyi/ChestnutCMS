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
