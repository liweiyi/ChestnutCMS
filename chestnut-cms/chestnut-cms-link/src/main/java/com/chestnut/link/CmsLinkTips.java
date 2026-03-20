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
