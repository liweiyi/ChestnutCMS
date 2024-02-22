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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.domain.SysWeChatConfig;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.mapper.SysWeChatConfigMapper;
import com.chestnut.system.service.ISysWeChatConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysWeChatConfigServiceImpl extends ServiceImpl<SysWeChatConfigMapper, SysWeChatConfig>
		implements ISysWeChatConfigService {

	private final static String CACHE_KEY_BACKEND = "sys:wechat:backend";

	private final RedisCache redisCache;

	@Override
	public SysWeChatConfig getBackendWeChatConfig() {
		return redisCache.getCacheObject(CACHE_KEY_BACKEND, () ->
				lambdaQuery().eq(SysWeChatConfig::getBackend, YesOrNo.YES).one()
		);
	}

	@Override
	public void addWeChatConfig(SysWeChatConfig config) {
		config.setConfigId(IdUtils.getSnowflakeId());
		config.setBackend(YesOrNo.NO);
		config.createBy(config.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void editWeChatConfig(SysWeChatConfig config) {
		SysWeChatConfig dbConfig = this.getById(config.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(config.getConfigId()));

		config.updateBy(config.getOperator().getUsername());
		this.updateById(config);
		this.redisCache.deleteObject(CACHE_KEY_BACKEND);
	}

	@Override
	public void deleteWeChatConfigs(List<Long> configIds) {
		this.removeByIds(configIds);
		this.redisCache.deleteObject(CACHE_KEY_BACKEND);
	}

	@Override
	public void changeConfigStatus(Long configId) {
		SysWeChatConfig config = this.getById(configId);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));

		if (!config.isForBackendLogin()) {
			this.lambdaUpdate().set(SysWeChatConfig::getBackend, YesOrNo.NO)
					.eq(SysWeChatConfig::getBackend, YesOrNo.YES)
					.update();
		}
		config.setBackend(config.isForBackendLogin() ? YesOrNo.NO : YesOrNo.YES);
		this.updateById(config);
		redisCache.deleteObject(CACHE_KEY_BACKEND);
	}
}
