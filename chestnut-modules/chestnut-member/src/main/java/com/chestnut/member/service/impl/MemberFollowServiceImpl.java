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
package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.core.impl.FollowMemberStatData;
import com.chestnut.member.core.impl.FollowerMemberStatData;
import com.chestnut.member.domain.MemberFollow;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.mapper.MemberFollowMapper;
import com.chestnut.member.service.IMemberFollowService;
import com.chestnut.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberFollowServiceImpl extends ServiceImpl<MemberFollowMapper, MemberFollow> implements IMemberFollowService {

    private final IMemberStatDataService memberStatDataService;

    private final RedissonClient redissonClient;

    private final AsyncTaskManager asyncTaskManager;

    @Override
    public void follow(long memberId, Long targetId) {
        Assert.isFalse(memberId == targetId, MemberErrorCode.CAN_NOT_FOLLOW_SELF::exception);
        RLock lock = redissonClient.getLock("MemberFollow-" + memberId);
        lock.lock();
        try {
            Long count = this.lambdaQuery().eq(MemberFollow::getMemberId, memberId)
                    .eq(MemberFollow::getFollowMemberId, targetId).count();
            Assert.isTrue(count == 0, MemberErrorCode.FOLLOWED::exception);

            MemberFollow mf = new MemberFollow();
            mf.setLogId(IdUtils.getSnowflakeId());
            mf.setMemberId(memberId);
            mf.setFollowMemberId(targetId);
            this.save(mf);
            this.updateMemberFollow(memberId, targetId, false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancelFollow(long memberId, Long targetId) {
        RLock lock = redissonClient.getLock("MemberFollow-" + memberId);
        lock.lock();
        try {
            Optional<MemberFollow> opt = this.lambdaQuery().eq(MemberFollow::getMemberId, memberId)
                    .eq(MemberFollow::getFollowMemberId, targetId).oneOpt();
            if (opt.isPresent()) {
                this.removeById(opt.get());
                this.updateMemberFollow(memberId, targetId ,true);
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateMemberFollow(final long memberId, final Long targetId, boolean cancel) {
        this.asyncTaskManager.execute(() -> {
            memberStatDataService.changeMemberStatData(memberId, FollowMemberStatData.TYPE, cancel ? -1 :1);
            memberStatDataService.changeMemberStatData(targetId, FollowerMemberStatData.TYPE, cancel ? -1 : 1);
        });
    }
}