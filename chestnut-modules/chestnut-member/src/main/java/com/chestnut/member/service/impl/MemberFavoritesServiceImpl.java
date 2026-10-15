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
package com.chestnut.member.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.core.IMemberFavoriteDataType;
import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.domain.vo.MemberFavoriteVO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.listener.event.AfterMemberCancelFavoriteEvent;
import com.chestnut.member.listener.event.AfterMemberFavoriteEvent;
import com.chestnut.member.listener.event.BeforeMemberFavoriteEvent;
import com.chestnut.member.mapper.MemberFavoritesMapper;
import com.chestnut.member.service.IMemberFavoritesService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 会员内容收藏服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberFavoritesServiceImpl extends ServiceImpl<MemberFavoritesMapper, MemberFavorites>
        implements IMemberFavoritesService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final RedissonClient redissonClient;

    private final static String SIGN_IN_LOCK_KEY = "cc:member:favorite:lock:";

    private final Map<String, IMemberFavoriteDataType> memberFavoritesMap;

    @Override
    public IMemberFavoriteDataType getMemberFavoriteDataType(String type) {
        return this.memberFavoritesMap.get(IMemberFavoriteDataType.BEAN_PREFIX + type);
    }

    @Override
    public IPage<MemberFavoriteVO> getMemberFavoritesPage(Long memberId, int pageNum, int pageSize) {
        Page<MemberFavorites> page = this.lambdaQuery().eq(MemberFavorites::getMemberId, memberId)
                .orderByDesc(MemberFavorites::getCreateTime, MemberFavorites::getLogId)
                .page(new Page<>(pageNum, pageSize, true));
        Map<String, List<MemberFavorites>> groups = page.getRecords().stream()
                .collect(Collectors.groupingBy(MemberFavorites::getDataType));
        Map<String, Map<String, IMemberFavoriteDataType.MemberFavoriteData>> dataByType = new HashMap<>();
        groups.forEach((type, favorites) -> {
            IMemberFavoriteDataType reader = this.getMemberFavoriteDataType(type);
            if (reader == null) return;
            List<String> ids = favorites.stream().map(favorite -> favorite.getDataId().toString()).distinct().toList();
            Map<String, IMemberFavoriteDataType.MemberFavoriteData> data = new HashMap<>();
            reader.getDataList(ids).stream().filter(item -> Objects.equals(type, item.type()))
                    .forEach(item -> data.put(item.id(), item));
            dataByType.put(type, data);
        });
        // Resolve by both type and ID, retaining the original page order and unavailable records.
        return page.convert(favorite -> {
            IMemberFavoriteDataType.MemberFavoriteData data = dataByType.getOrDefault(favorite.getDataType(), Map.of())
                    .get(favorite.getDataId().toString());
            return MemberFavoriteVO.from(favorite, data == null ? null : data.name());
        });
    }

    @Override
    public List<MemberFavorites> getMemberFavorites(Long memberId, String dataType, Integer limit, Long offset) {
        return this.lambdaQuery()
                .eq(MemberFavorites::getDataType, dataType)
                .eq(MemberFavorites::getMemberId, memberId)
                .lt(IdUtils.validate(offset), MemberFavorites::getLogId, offset)
                .orderByDesc(MemberFavorites::getLogId)
                .page(Page.of(1, limit, false))
                .getRecords();
    }

    @Override
    public void favorite(Long memberId, String dataType, Long dataId) {
        RLock lock = redissonClient.getLock(SIGN_IN_LOCK_KEY + memberId);
        try {
            lock.lock();

            Long count = this.lambdaQuery()
                    .eq(MemberFavorites::getMemberId, memberId)
                    .eq(MemberFavorites::getDataType, dataType)
                    .eq(MemberFavorites::getDataId, dataId)
                    .count();
            Assert.isTrue(count == 0, MemberErrorCode.FAVORITED::exception);

            this.applicationContext.publishEvent(new BeforeMemberFavoriteEvent(this, dataType, dataId));

            MemberFavorites favorites = new MemberFavorites();
            favorites.setLogId(IdUtils.getSnowflakeId());
            favorites.setMemberId(memberId);
            favorites.setDataType(dataType);
            favorites.setDataId(dataId);
            favorites.setCreateTime(LocalDateTime.now());
            this.save(favorites);

            this.applicationContext.publishEvent(new AfterMemberFavoriteEvent(this, favorites));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancelFavorite(Long memberId, String dataType, Long dataId) {
        RLock lock = redissonClient.getLock(SIGN_IN_LOCK_KEY + memberId);
        try {
            lock.lock();

            Optional<MemberFavorites> opt = this.lambdaQuery()
                    .eq(MemberFavorites::getMemberId, memberId)
                    .eq(MemberFavorites::getDataType, dataType)
                    .eq(MemberFavorites::getDataId, dataId)
                    .oneOpt();
            Assert.isTrue(opt.isPresent(), MemberErrorCode.NOT_FAVORITED::exception);

            MemberFavorites memberFavorites = opt.get();
            this.removeById(memberFavorites);

            this.applicationContext.publishEvent(new AfterMemberCancelFavoriteEvent(this, memberFavorites));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
