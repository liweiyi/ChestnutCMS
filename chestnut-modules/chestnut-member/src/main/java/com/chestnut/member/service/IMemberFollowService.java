package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberFollow;

public interface IMemberFollowService extends IService<MemberFollow> {

    /**
     * 关注 用户
     *
     * @param memberId
     * @param targetId
     */
    void follow(long memberId, Long targetId);

    /**
     * 取消关注  用户
     *
     * @param memberId
     * @param targetId
     */
    void cancelFollow(long memberId, Long targetId);
}