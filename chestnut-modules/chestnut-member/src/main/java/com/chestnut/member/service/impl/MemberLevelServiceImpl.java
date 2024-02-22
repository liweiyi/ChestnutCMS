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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.member.domain.MemberLevel;
import com.chestnut.member.mapper.MemberLevelMapper;
import com.chestnut.member.service.IMemberLevelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelMapper, MemberLevel> implements IMemberLevelService {

	private static final String CACHE_PREFIX = "memberlevel:";

	private final RedisCache redisCache;

	@Override
	public MemberLevel getMemberLevel(Long memberId, String levelType) {
		return this.redisCache.getCacheObject(CACHE_PREFIX + memberId + ":" +levelType, () -> {
			MemberLevel ml = this.lambdaQuery().eq(MemberLevel::getMemberId, memberId)
					.eq(MemberLevel::getLevelType, levelType).one();
			if (Objects.isNull(ml)) {
				ml = new MemberLevel();
				ml.setDataId(IdUtils.getSnowflakeId());
				ml.setMemberId(memberId);
				ml.setLevelType(levelType);
				ml.setLevel(0);
				ml.setExp(0L);
				this.save(ml);
			}
			return ml;
		});
	}

	@Override
	public List<MemberLevel> getMemberLevels(Long memberId) {
		Map<String, MemberLevel> cacheMap = this.redisCache.getCacheMap(CACHE_PREFIX + memberId, () -> {
			return this.lambdaQuery().eq(MemberLevel::getMemberId, memberId).list()
					.stream().collect(Collectors.toMap(MemberLevel::getLevelType, ml -> ml));
		});
		return cacheMap.values().stream().toList();
	}
}