package com.chestnut.member.core.impl;

import com.chestnut.member.core.IMemberStatData;
import org.springframework.stereotype.Component;

/**
 * 关注数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberStatData.BEAN_PREFIX + FollowMemberStatData.TYPE)
public class FollowMemberStatData implements IMemberStatData {

    public static final String TYPE = "follow";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getField() {
        return "intValue2";
    }
}
