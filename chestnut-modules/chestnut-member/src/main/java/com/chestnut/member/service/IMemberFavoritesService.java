/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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