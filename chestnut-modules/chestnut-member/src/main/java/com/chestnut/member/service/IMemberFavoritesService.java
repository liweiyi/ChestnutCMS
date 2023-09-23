package com.chestnut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.member.domain.MemberFavorites;

import java.util.List;

public interface IMemberFavoritesService extends IService<MemberFavorites> {

    /**
     * 获取指定用户收藏内容列表
     *
     * @param memberId
     * @param dataType
     * @param limit
     * @param offset
     * @return
     */
    List<MemberFavorites> getMemberFavorites(Long memberId, String dataType, Integer limit, Long offset);

    /**
     * 收藏
     *
     * @param memberId
     * @param dataType
     * @param dataId
     */
    void favorite(Long memberId, String dataType, Long dataId);

    /**
     * 取消收藏
     *
     * @param memberId
     * @param dataType
     * @param dataId
     */
    void cancelFavorite(Long memberId, String dataType, Long dataId);
}