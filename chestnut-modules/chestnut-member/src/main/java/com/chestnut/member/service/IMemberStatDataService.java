package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberFollow;
import com.chestnut.member.domain.MemberStatData;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.service.impl.MemberStatDataServiceImpl;

public interface IMemberStatDataService extends IService<MemberStatData> {

    /**
     * 会员基础数据缓存
     *
     * @param memberId
     * @return
     */
    MemberCache getMemberCache(Long memberId);

    void removeMemberCache(Long memberId);

    /**
     * 更新统计数据
     *
     * @param memberId
     * @param statDataType
     * @param delta
     */
    void changeMemberStatData(Long memberId, String statDataType, Integer delta);
}