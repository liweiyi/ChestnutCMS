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
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.core.IMemberStatData;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.MemberStatData;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.mapper.MemberStatDataMapper;
import com.chestnut.member.service.IMemberService;
import com.chestnut.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberStatDataServiceImpl extends ServiceImpl<MemberStatDataMapper, MemberStatData> implements IMemberStatDataService {

    private static final String CACHE_PREFIX = "cc:member:";

    private final IMemberService memberService;

    private final RedissonClient redissonClient;

    private final RedisCache redisCache;

    private final Map<String, IMemberStatData> memberStatDataTypes;

    @Override
    public MemberCache getMemberCache(Long memberId) {
        return this.redisCache.getCacheObject(CACHE_PREFIX + memberId, () -> {
            Member member = memberService.getById(memberId);
            if (member == null) {
                return null;
            }
            MemberCache memberCache = new MemberCache();
            memberCache.setMemberId(memberId);
            if (StringUtils.isNotEmpty(member.getNickName())) {
                memberCache.setDisplayName(member.getNickName());
            } else {
                memberCache.setDisplayName(member.getUserName());
            }
            memberCache.setCover(member.getCover());
            memberCache.setAvatar(member.getAvatar());
            memberCache.setSlogan(member.getSlogan());
            MemberStatData data = getById(memberId);
            memberStatDataTypes.values().forEach(t -> {
                memberCache.getStat().put(t.getType(), Objects.nonNull(data) ? data.getValue(t.getField()) : 0);
            });
            return memberCache;
        });
    }

    @Override
    public void removeMemberCache(Long memberId) {
        this.redisCache.deleteObject(CACHE_PREFIX + memberId);
    }

    @Async
    @Override
    public void changeMemberStatData(Long memberId, String statDataType, Integer delta) {
        IMemberStatData memberStatData = this.getMemberStatData(statDataType);
        if (memberStatData == null) {
            return;
        }
        Member member = this.memberService.getById(memberId);
        if (member == null) {
            return;
        }
        RLock lock = redissonClient.getLock("MemberStatData_" + memberId);
        lock.lock();
        try {
            MemberStatData data = this.getById(memberId);
            if (data == null) {
                data = new MemberStatData();
                data.setMemberId(memberId);
            }

            Integer value = data.getValue(memberStatData.getField());
            if (value == null) {
                value = 0;
            }
            value += delta;
            data.setValue(memberStatData.getField(), value);
            this.saveOrUpdate(data);
            this.redisCache.deleteObject(CACHE_PREFIX + memberId);
        } finally {
            lock.unlock();
        }
    }

    private IMemberStatData getMemberStatData(String type) {
        return this.memberStatDataTypes.get(IMemberStatData.BEAN_PREFIX + type);
    }
}