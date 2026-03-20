package com.chestnut.block;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsBlockTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsBlockTips implements TipMessage {

    /**
     * 正在处理页面部件区块类型数据
     */
    IMPORTING_BLOCK,
    ;

    @Override
    public String value() {
        return "TIP.CMS.BLOCK." + this.name();
    }
}
