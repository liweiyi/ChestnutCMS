package com.chestnut.member.core.impl;

import com.chestnut.member.core.IMemberStatData;
import org.springframework.stereotype.Component;

/**
 * 粉丝数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberStatData.BEAN_PREFIX + FollowerMemberStatData.TYPE)
public class FollowerMemberStatData implements IMemberStatData {

    public static final String TYPE = "follower";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getField() {
        return "intValue1";
    }
}
