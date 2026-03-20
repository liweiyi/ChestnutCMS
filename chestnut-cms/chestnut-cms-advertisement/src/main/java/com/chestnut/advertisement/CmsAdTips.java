package com.chestnut.advertisement;

import com.chestnut.common.exception.TipMessage;

public enum CmsAdTips implements TipMessage {

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
