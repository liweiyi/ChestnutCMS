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
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.domain.MemberLike;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.listener.event.AfterMemberCancelLikeEvent;
import com.chestnut.member.listener.event.AfterMemberLikeEvent;
import com.chestnut.member.listener.event.BeforeMemberLikeEvent;
import com.chestnut.member.mapper.MemberLikeMapper;
import com.chestnut.member.service.IMemberLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会员点赞服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberLikeServiceImpl extends ServiceImpl<MemberLikeMapper, MemberLike>
        implements IMemberLikeService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void like(Long memberId, String dataType, Long dataId) {
        Long count = this.lambdaQuery()
                .eq(MemberLike::getMemberId, memberId)
                .eq(MemberLike::getDataType, dataType)
                .eq(MemberLike::getDataId, dataId)
                .count();
        Assert.isTrue(count == 0, MemberErrorCode.LIKED::exception);

        this.applicationContext.publishEvent(new BeforeMemberLikeEvent(this, dataType, dataId));

        MemberLike like = new MemberLike();
        like.setLogId(IdUtils.getSnowflakeId());
        like.setMemberId(memberId);
        like.setDataType(dataType);
        like.setDataId(dataId);
        like.setCreateTime(LocalDateTime.now());
        this.save(like);

        this.applicationContext.publishEvent(new AfterMemberLikeEvent(this, like));
    }

    @Override
    public void cancelLike(Long memberId, String dataType, Long dataId) {
        Optional<MemberLike> opt = this.lambdaQuery()
                .eq(MemberLike::getMemberId, memberId)
                .eq(MemberLike::getDataType, dataType)
                .eq(MemberLike::getDataId, dataId)
                .oneOpt();
        Assert.isTrue(opt.isPresent(), MemberErrorCode.NOT_LIKED::exception);

        MemberLike memberLike = opt.get();
        this.removeById(memberLike);

        this.applicationContext.publishEvent(new AfterMemberCancelLikeEvent(this, memberLike));
    }

    @Override
    public List<MemberLike> getMemberLikes(Long memberId, String dataType, Integer limit, Long offset) {
        List<MemberLike> list = this.lambdaQuery()
                .eq(MemberLike::getDataType, dataType)
                .eq(MemberLike::getMemberId, memberId)
                .lt(IdUtils.validate(offset), MemberLike::getLogId, offset)
                .orderByDesc(MemberLike::getLogId)
                .last("limit " + limit)
                .list();
        return list;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
