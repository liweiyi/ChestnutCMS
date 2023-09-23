package com.chestnut.member.core;

import com.chestnut.member.domain.MemberStatData;

/**
 * 会员统计数据记录
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberStatData {

    String BEAN_PREFIX = "MemberDataStat_";

    String getType();

    /**
     * 数据存储字段名
     *
     * @see MemberStatData
     * @return
     */
    String getField();
}
