/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.domain.SysUserBinding;
import com.chestnut.system.mapper.SysUserBindingMapper;
import com.chestnut.system.service.ISysUserBindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SysUserBindingServiceImpl extends ServiceImpl<SysUserBindingMapper, SysUserBinding>
		implements ISysUserBindingService {

	private final static String CACHE_KEY = "sys:user:binding:";

	private final RedisCache redisCache;
	
	@Override
    public Long getUserId(String bindingType, String openId) {
        Long userId = this.redisCache.getLongCacheValue(CACHE_KEY + bindingType + ":" + openId);
        if (!IdUtils.validate(userId)) {
            Optional<SysUserBinding> opt = this.lambdaQuery().eq(SysUserBinding::getBindingType, bindingType)
                    .eq(SysUserBinding::getOpenId, openId)
                    .select(SysUserBinding::getUserId)
                    .oneOpt();
            if (opt.isPresent()) {
                userId = opt.get().getUserId();
                this.redisCache.setCacheObject(CACHE_KEY + bindingType + ":" + openId, userId,
                        3600L, TimeUnit.SECONDS);
            }
        }
        return userId;
    }
}
