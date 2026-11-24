package com.chestnut.cms.member;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsMemberTips implements TipMessage {

    /** 只能删除待审核的初稿 */
    DRAFT_DELETE_ONLY,
    ;

    @Override
    public String value() {
        return "TIP.CMS.MEMBER." + name();
    }
}
