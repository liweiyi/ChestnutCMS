package com.chestnut.seo;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsSeoTips implements TipMessage {

    /** 正在生成Sitemap：{0} */
    BUILDING_SITEMAP,

    /** Sitemap生成完成 */
    SITEMAP_COMPLETED,
    ;

    @Override
    public String value() {
        return "TIP.CMS.SEO." + name();
    }
}
