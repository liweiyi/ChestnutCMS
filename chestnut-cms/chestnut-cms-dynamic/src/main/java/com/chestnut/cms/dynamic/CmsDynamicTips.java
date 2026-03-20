package com.chestnut.cms.dynamic;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsDynamicTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsDynamicTips implements TipMessage {

    /**
     * 正在导出自定义动态模板页面数据
     */
    EXPORTING_DYNAMIC_TEMPLATE_PAGE,

    /**
     * 正在导入自定动态模板页面数据
     */
    IMPORTING_DYNAMIC_TEMPLATE_PAGE,

    /**
     * 导入自定义动态模板页面`{0}`失败：{1}"
     */
    IMPORT_DYNAMIC_TEMPLATE_PAGE_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.DYNAMIC." + this.name();
    }
}
