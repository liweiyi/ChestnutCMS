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
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.dto.CreateLoginConfigRequest;
import com.chestnut.system.domain.dto.UpdateLoginConfigRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.security.ILoginType;
import com.chestnut.system.mapper.SysLoginConfigMapper;
import com.chestnut.system.service.ILoginConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginConfigServiceImpl extends ServiceImpl<SysLoginConfigMapper, SysLoginConfig>
		implements ILoginConfigService {

	private final static String CACHE_KEY = "sys:login:config:";

    private final Map<String, ILoginType> loginTypeMap;

	private final RedisCache redisCache;

    @Override
    public ILoginType getLoginType(String type) {
        ILoginType loginType = this.loginTypeMap.get(ILoginType.BEAN_PREFIX + type);
        Assert.notNull(loginType, () -> SysErrorCode.UNSUPPORTED_LOGIN_TYPE.exception(type));
        return loginType;
    }
	
	@Override
	public SysLoginConfig getLoginConfig(Long configId) {
        SysLoginConfig config = this.redisCache.getCacheObject(CACHE_KEY + configId, SysLoginConfig.class, () ->
                getById(configId)
        );
        Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));
        return config;
    }

	@Override
	public void addConfig(CreateLoginConfigRequest req) {
        SysLoginConfig config = new SysLoginConfig();
		BeanUtils.copyProperties(req, config);
		config.setConfigId(IdUtils.getSnowflakeId());
		config.setStatus(EnableOrDisable.ENABLE);
		config.createBy(req.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void saveConfig(UpdateLoginConfigRequest req) {
        SysLoginConfig dbConfig = this.getById(req.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getConfigId()));

        ILoginType loginType = this.getLoginType(dbConfig.getType());
        loginType.updateConfigProps(dbConfig.getConfigProps(), req.getConfigProps());

        dbConfig.setConfigName(req.getConfigName());
        dbConfig.setConfigDesc(req.getConfigDesc());
        dbConfig.setStatus(req.getStatus());
        dbConfig.setConfigProps(req.getConfigProps());
		dbConfig.updateBy(req.getOperator().getUsername());
		this.updateById(dbConfig);
		this.redisCache.deleteObject(CACHE_KEY + dbConfig.getConfigId());
	}

	@Override
	public void deleteConfigs(List<Long> configIds) {
        List<SysLoginConfig> configs = this.listByIds(configIds);
        this.removeByIds(configIds);
        for (SysLoginConfig config : configs) {
            this.redisCache.deleteObject(CACHE_KEY + config.getConfigId());
        }
	}
}
