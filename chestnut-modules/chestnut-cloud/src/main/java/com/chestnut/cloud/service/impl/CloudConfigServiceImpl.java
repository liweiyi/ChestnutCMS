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
package com.chestnut.cloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.cloud.domain.CcCloudConfig;
import com.chestnut.cloud.domain.dto.CreateCloudConfigRequest;
import com.chestnut.cloud.domain.dto.UpdateCloudConfigRequest;
import com.chestnut.cloud.mapper.CloudConfigMapper;
import com.chestnut.cloud.service.ICloudConfigService;
import com.chestnut.common.cloud.CloudErrorCode;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudConfigServiceImpl extends ServiceImpl<CloudConfigMapper, CcCloudConfig>
		implements ICloudConfigService {

	private final static String CACHE_KEY = "cc:cloud:config:";

	private final RedisCache redisCache;

    private final Map<String, ICloudProvider> cloudProviderMap;

    @Override
    public ICloudProvider getCloudProvider(String cloudProviderId) {
        ICloudProvider provider = cloudProviderMap.get(ICloudProvider.BEAN_PREFIX + cloudProviderId);
        Assert.notNull(provider, () -> CloudErrorCode.UNSUPPORTED_CLOUD_PROVIDER.exception(cloudProviderId));
        return provider;
    }

	
	@Override
	public CcCloudConfig getCloudConfig(Long configId) {
        CcCloudConfig config = this.redisCache.getCacheObject(CACHE_KEY + configId, CcCloudConfig.class, () ->
                getById(configId)
        );
        Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));
        return config;
    }

	@Override
	public void addConfig(CreateCloudConfigRequest req) {
        CcCloudConfig config = new CcCloudConfig();
		BeanUtils.copyProperties(req, config);
		config.setConfigId(IdUtils.getSnowflakeId());
		config.setStatus(EnableOrDisable.ENABLE);
		config.createBy(req.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void saveConfig(UpdateCloudConfigRequest req) {
        CcCloudConfig dbConfig = this.getById(req.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getConfigId()));

        ICloudProvider cloudProvider = this.getCloudProvider(dbConfig.getType());
        cloudProvider.updateConfigProps(dbConfig.getConfigProps(), req.getConfigProps());

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
        List<CcCloudConfig> configs = this.listByIds(configIds);
        this.removeByIds(configIds);
        for (CcCloudConfig config : configs) {
            this.redisCache.deleteObject(CACHE_KEY + config.getConfigId());
        }
	}
}
