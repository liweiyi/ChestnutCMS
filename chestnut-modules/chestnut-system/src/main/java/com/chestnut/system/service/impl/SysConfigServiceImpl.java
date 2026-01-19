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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysConfig;
import com.chestnut.system.domain.dto.CreateConfigRequest;
import com.chestnut.system.domain.dto.UpdateConfigRequest;
import com.chestnut.system.fixed.FixedConfigUtils;
import com.chestnut.system.mapper.SysConfigMapper;
import com.chestnut.system.service.ISysConfigService;
import com.chestnut.system.service.ISysI18nDictService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 参数配置 服务层实现
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig>
		implements ISysConfigService, CommandLineRunner {

	private final RedisCache redisCache;

    private final RedissonClient redissonClient;

	private final ISysI18nDictService i18nDictService;

	@Override
	public String selectConfigByKey(String configKey) {
        return redisCache.getCacheObject(getCacheKey(configKey), String.class, () -> {
			SysConfig one = this.lambdaQuery().eq(SysConfig::getConfigKey, configKey).one();
			return Objects.nonNull(one) ? one.getConfigValue() : null;
		});
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertConfig(CreateConfigRequest req) {
		boolean checkConfigKeyUnique = this.checkConfigKeyUnique(req.getConfigKey(), null);
		Assert.isTrue(checkConfigKeyUnique, () -> CommonErrorCode.DATA_CONFLICT.exception(req.getConfigKey()));

		SysConfig config = new SysConfig();
		config.setConfigId(IdUtils.getSnowflakeId());
		config.setConfigKey(req.getConfigKey());
		config.setConfigName(req.getConfigName());
		config.setConfigValue(req.getConfigValue());
		config.setRemark(req.getRemark());
		config.createBy(req.getOperator().getUsername());
		this.save(config);

		i18nDictService.saveOrUpdate(LocaleContextHolder.getLocale().toLanguageTag(),
				langKey(req.getConfigKey()), req.getConfigName());
		redisCache.setCacheObject(getCacheKey(req.getConfigKey()), req.getConfigValue());
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void updateConfig(UpdateConfigRequest req) {
		SysConfig dbConfig = this.getById(req.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("configId", req.getConfigId()));
		// 系统固定配置参数不能修改键名
		String oldConfigKey = dbConfig.getConfigKey();
		if (FixedConfigUtils.isFixedConfig(oldConfigKey) && !StringUtils.equals(oldConfigKey, req.getConfigKey())) {
			throw CommonErrorCode.FIXED_CONFIG_UPDATE.exception(oldConfigKey);
		}
		// 键名是否重复
		boolean checkConfigKeyUnique = this.checkConfigKeyUnique(req.getConfigKey(), req.getConfigId());
		Assert.isTrue(checkConfigKeyUnique, () -> CommonErrorCode.DATA_CONFLICT.exception(req.getConfigKey()));

		dbConfig.setConfigKey(req.getConfigKey());
		dbConfig.setConfigName(req.getConfigName());
		dbConfig.setConfigValue(req.getConfigValue());
		dbConfig.setRemark(req.getRemark());
		dbConfig.updateBy(req.getOperator().getUsername());
		if (this.updateById(dbConfig)) {
			redisCache.setCacheObject(getCacheKey(dbConfig.getConfigKey()), dbConfig.getConfigValue());
			if (!StringUtils.equals(oldConfigKey, dbConfig.getConfigKey())) {
				redisCache.deleteObject(getCacheKey(oldConfigKey));
				i18nDictService.changeLangKey(langKey(oldConfigKey),
						langKey(dbConfig.getConfigKey()), false);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void deleteConfigByIds(List<Long> configIds) {
		for (Long configId : configIds) {
			SysConfig config = this.getById(configId);
			boolean fixed = FixedConfigUtils.isFixedConfig(config.getConfigKey());
			Assert.isFalse(fixed, () -> CommonErrorCode.FIXED_CONFIG_DEL.exception(config.getConfigKey()));

			this.removeById(configId);
			redisCache.deleteObject(getCacheKey(config.getConfigKey()));
			// 删除国际化配置
			this.i18nDictService.deleteByLangKey(langKey(config.getConfigKey()), false);
		}
	}

	private String langKey(String configKey) {
		return "CONFIG." + configKey;
	}

	@Override
	public void loadingConfigCache() {
		this.list().forEach(config -> {
			redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
		});
	}

	@Override
	public void clearConfigCache() {
		Collection<String> keys = redisCache.keys(SysConstants.CACHE_SYS_CONFIG_KEY + "*");
		redisCache.deleteObjects(keys);
	}

	@Override
	public void resetConfigCache() {
		clearConfigCache();
		loadingConfigCache();
	}

	private boolean checkConfigKeyUnique(String configKey, Long configId) {
		long count = this.count(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey)
				.ne(IdUtils.validate(configId), SysConfig::getConfigId, configId));
		return count == 0;
	}

	private String getCacheKey(String configKey) {
		return SysConstants.CACHE_SYS_CONFIG_KEY + configKey;
	}

	@Override
	public void run(String... args) throws Exception {
        RLock lock = redissonClient.getLock("cc:dict:init_fixed_dict");
        lock.lock();
        try {
            this.resetConfigCache();
            // 不包含在keys中的需要保存进数据库
            FixedConfigUtils.allConfigs().forEach(fc -> {
                if (!this.redisCache.hasKey(getCacheKey(fc.getKey()))) {
                    SysConfig config = new SysConfig();
                    config.setConfigId(IdUtils.getSnowflakeId());
                    config.setConfigKey(fc.getKey());
                    config.setConfigName(I18nUtils.get(fc.getName()));
                    config.setConfigValue(fc.getDefaultValue());
                    config.setRemark(fc.getRemark());
                    config.createBy(SysConstants.SYS_OPERATOR);
                    this.save(config);
                    // 更新缓存
                    redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
                }
            });
        } finally {
            lock.unlock();
        }
	}
}
