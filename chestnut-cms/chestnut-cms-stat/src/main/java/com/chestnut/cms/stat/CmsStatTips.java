package com.chestnut.cms.stat;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsStatTips implements TipMessage {

    /** 获取百度统计报表失败，请检查统计配置或稍后重试 */
    BAIDU_REPORT_FAILED,
    ;

    @Override
    public String value() {
        return "TIP.CMS.STAT." + name();
    }
}
