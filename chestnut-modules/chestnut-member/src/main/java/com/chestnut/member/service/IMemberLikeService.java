package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberLike;

import java.util.List;

public interface IMemberLikeService extends IService<MemberLike> {

    /**
     * 点赞
     *
     * @param memberId
     * @param dataType
     * @param dataId
     */
    void like(Long memberId, String dataType, Long dataId);

    /**
     * 取消点赞
     *
     * @param memberId
     * @param dataType
     * @param dataId
     */
    void cancelLike(Long memberId, String dataType, Long dataId);

    List<MemberLike> getMemberLikes(Long memberId, String dataType, Integer limit, Long offset);
}