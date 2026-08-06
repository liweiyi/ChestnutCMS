/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.member.domain.MemberFollow;

public interface IMemberFollowService extends IService<MemberFollow> {

    /**
     * 关注 用户
     *
     * @param memberId Member id.
     * @param targetId The follow member id.
     */
    void follow(long memberId, Long targetId);

    /**
     * 取消关注  用户
     *
     * @param memberId Member id.
     * @param targetId The cancel follow member id.
     */
    void cancelFollow(long memberId, Long targetId);
}