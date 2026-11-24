package com.chestnut.cms.cdn;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsCdnTips implements TipMessage {

    /** 未配置发布通道 */
    PUBLISH_PIPE_MISSING,
    ;

    @Override
    public String value() {
        return "TIP.CMS.CDN." + name();
    }
}
