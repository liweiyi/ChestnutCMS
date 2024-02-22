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
import com.chestnut.system.domain.SysI18nDict;
import com.chestnut.system.fixed.FixedConfigUtils;
import com.chestnut.system.mapper.SysConfigMapper;
import com.chestnut.system.service.ISysConfigService;
import com.chestnut.system.service.ISysI18nDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

	private final ISysI18nDictService i18nDictService;

	/**
	 * 根据键名查询参数配置信息
	 * 
	 * @param configKey 参数key
	 * @return 参数键值
	 */
	@Override
	public String selectConfigByKey(String configKey) {
		String configValue = redisCache.getCacheObject(getCacheKey(configKey), () -> {
			SysConfig one = this.lambdaQuery().eq(SysConfig::getConfigKey, configKey).one();
			return Objects.nonNull(one) ? one.getConfigValue() : null;
		});
		return configValue;
	}

	/**
	 * 新增参数配置
	 * 
	 * @param config 参数配置信息
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertConfig(SysConfig config) {
		boolean checkConfigKeyUnique = this.checkConfigKeyUnique(config);
		Assert.isTrue(checkConfigKeyUnique, () -> CommonErrorCode.DATA_CONFLICT.exception(config.getConfigKey()));

		config.setConfigId(IdUtils.getSnowflakeId());
		config.setCreateTime(LocalDateTime.now());
		this.save(config);

		SysI18nDict i18nDict = new SysI18nDict();
		i18nDict.setLangKey("CONFIG." + config.getConfigKey());
		i18nDict.setLangTag(LocaleContextHolder.getLocale().toLanguageTag());
		i18nDict.setLangValue(config.getConfigName());
		i18nDictService.batchSaveI18nDicts(List.of(i18nDict));

		redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
	}

	/**
	 * 修改参数配置
	 * 
	 * @param config 参数配置信息
	 * @return 结果
	 */
	@Override
	public void updateConfig(SysConfig config) {
		SysConfig dbConfig = this.getById(config.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("configId", config.getConfigId()));
		// 系统固定配置参数不能修改键名
		String oldConfigKey = dbConfig.getConfigKey();
		if (FixedConfigUtils.isFixedConfig(oldConfigKey) && !StringUtils.equals(oldConfigKey, config.getConfigKey())) {
			throw CommonErrorCode.FIXED_CONFIG_UPDATE.exception(oldConfigKey);
		}
		// 键名是否重复
		boolean checkConfigKeyUnique = this.checkConfigKeyUnique(config);
		Assert.isTrue(checkConfigKeyUnique, () -> CommonErrorCode.DATA_CONFLICT.exception(config.getConfigKey()));

		config.setUpdateTime(LocalDateTime.now());
		if (this.updateById(config)) {
			redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
		}
	}

	/**
	 * 批量删除参数信息
	 * 
	 * @param configIds 需要删除的参数ID
	 */
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
			this.i18nDictService.remove(new LambdaQueryWrapper<SysI18nDict>().eq(SysI18nDict::getLangKey, "CONFIG." + config.getConfigKey()));
		}
	}

	/**
	 * 加载参数缓存数据
	 */
	@Override
	public void loadingConfigCache() {
		this.list().forEach(config -> {
			redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
		});
	}

	/**
	 * 清空参数缓存数据
	 */
	@Override
	public void clearConfigCache() {
		Collection<String> keys = redisCache.keys(SysConstants.CACHE_SYS_CONFIG_KEY + "*");
		redisCache.deleteObject(keys);
	}

	/**
	 * 重置参数缓存数据
	 */
	@Override
	public void resetConfigCache() {
		clearConfigCache();
		loadingConfigCache();
	}

	/**
	 * 校验参数键名是否唯一
	 * 
	 * @param config 参数配置信息
	 * @return 结果
	 */
	private boolean checkConfigKeyUnique(SysConfig config) {
		long count = this.count(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, config.getConfigKey())
				.ne(IdUtils.validate(config.getConfigId()), SysConfig::getConfigId, config.getConfigId()));
		return count == 0;
	}

	/**
	 * 设置cache key
	 * 
	 * @param configKey 参数键
	 * @return 缓存键key
	 */
	private String getCacheKey(String configKey) {
		return SysConstants.CACHE_SYS_CONFIG_KEY + configKey;
	}

	@Override
	public void run(String... args) throws Exception {
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
	}

}
