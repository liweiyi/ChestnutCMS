package com.chestnut.article;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsArticleTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsArticleTips implements TipMessage {

    /**
     * 正在导出文章详情数据
     */
    EXPORTING_ARTICLE,

    /**
     * 正在导入文章详情数据
     */
    IMPORTING_ARTICLE,

    /**
     * 导入文章详情数据`{0}`失败：{1}
     */
    IMPORT_ARTICLE_FAIL,
    ;


    @Override
    public String value() {
        return "TIP.CMS.ARTICLE." + this.name();
    }
}
