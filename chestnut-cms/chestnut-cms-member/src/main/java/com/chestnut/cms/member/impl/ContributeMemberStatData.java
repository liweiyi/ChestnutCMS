package com.chestnut.cms.member.impl;

import com.chestnut.member.core.IMemberStatData;
import org.springframework.stereotype.Component;

/**
 * 会员投稿数统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberStatData.BEAN_PREFIX + ContributeMemberStatData.TYPE)
public class ContributeMemberStatData implements IMemberStatData {

    public static final String TYPE = "contribute";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getField() {
        return "intValue4";
    }
}
