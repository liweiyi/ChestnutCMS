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
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.PasswordRetryStrategy;
import com.chestnut.system.fixed.dict.PasswordRule;
import com.chestnut.system.fixed.dict.PasswordSensitive;
import com.chestnut.system.mapper.SysSecurityConfigMapper;
import com.chestnut.system.security.ISecurityUser;
import com.chestnut.system.service.ISecurityConfigService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SecurityConfigServiceImpl extends ServiceImpl<SysSecurityConfigMapper, SysSecurityConfig>
		implements ISecurityConfigService {

	private final static String CACHE_KEY_CONFIG = "sys:security:config";

	private final static String CACHE_KEY_PWD_RETRY = "sys:security:pwdretry:";

	private final RedisCache redisCache;
	
	@Override
	public SysSecurityConfig getSecurityConfig() {
		return this.redisCache.getCacheObject(CACHE_KEY_CONFIG, () ->
			lambdaQuery().eq(SysSecurityConfig::getStatus, EnableOrDisable.ENABLE).one()
		);
	}

	@Override
	public void addConfig(SysSecurityConfig config) {
		config.setConfigId(IdUtils.getSnowflakeId());
		config.setStatus(EnableOrDisable.DISABLE); // 默认不开启
		config.createBy(config.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void saveConfig(SysSecurityConfig config) {
		SysSecurityConfig dbConfig = this.getById(config.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(config.getConfigId()));

		config.updateBy(config.getOperator().getUsername());
		this.updateById(config);
		// 清除安全配置缓存，实际使用的时候重新缓存
		this.redisCache.deleteObject(CACHE_KEY_CONFIG);
	}

	@Override
	public void deleteConfigs(List<Long> configIds) {
		this.removeByIds(configIds);
		this.redisCache.deleteObject(CACHE_KEY_CONFIG);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void changeConfigStatus(Long configId) {
		SysSecurityConfig config = this.getById(configId);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));

		if (!config.isEnable()) {
			this.lambdaUpdate().set(SysSecurityConfig::getStatus, EnableOrDisable.DISABLE)
					.eq(SysSecurityConfig::getStatus, EnableOrDisable.ENABLE)
					.update();
		}
		config.setStatus(config.isEnable() ? EnableOrDisable.DISABLE : EnableOrDisable.ENABLE);
		this.updateById(config);
		// 清除安全配置缓存，实际使用的时候重新缓存
		redisCache.deleteObject(CACHE_KEY_CONFIG);
	}

	@Override
	public void validPassword(ISecurityUser user, String password) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.nonNull(securityConfig) && securityConfig.isEnable()) {
			// 最大长度
			boolean valid = securityConfig.getPasswordLenMax() > 0
					&& password.length() <= securityConfig.getPasswordLenMax();
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 最小长度
			valid = securityConfig.getPasswordLenMin() > 0 && password.length() >= securityConfig.getPasswordLenMin();
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 校验规则检查
			valid = PasswordRule.match(securityConfig.getPasswordRule(), password);
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 敏感字符检查
			valid = PasswordSensitive.check(securityConfig.getPasswordSensitive(), password, user);
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 弱密码检查
			String[] weakPasswords = securityConfig.getWeakPasswords().split("\n");
			valid = StringUtils.isEmpty(weakPasswords) || !StringUtils.equalsAny(password, weakPasswords);
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
		}
	}

	@Override
	public void forceModifyPwdAfterResetPwd(ISecurityUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.nonNull(securityConfig) && securityConfig.isEnable()
				&& securityConfig.checkForceModifyPwdAfterReset()) {
			user.forceModifyPassword();
		}
	}

	@Override
	public void forceModifyPwdAfterUserAdd(ISecurityUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.nonNull(securityConfig) && securityConfig.isEnable()
				&& securityConfig.checkForceModifyPwdAfterAdd()) {
			user.forceModifyPassword();
		}
	}

	/**
	 * 
	 */
	@Override
	public boolean processLoginPasswordError(ISecurityUser user) {
		SysSecurityConfig config = this.getSecurityConfig();
		if (Objects.nonNull(config) && config.isEnable()) {
			String cacheKey = user.getType() + "_" + user.getUserId();
			// 缓存更新
			LoginPwdRetry lpe = this.redisCache.getCacheMapValue(CACHE_KEY_PWD_RETRY, cacheKey);
			if (Objects.isNull(lpe)) {
				lpe = new LoginPwdRetry(cacheKey);
			}
			lpe.inc();
			this.redisCache.setCacheMapValue(CACHE_KEY_PWD_RETRY, cacheKey, lpe);
			// 执行策略
			int passwordRetryLimit = config.getPasswordRetryLimit();
			if (passwordRetryLimit > 0 && lpe.getNum() >= passwordRetryLimit) {
				// 达到指定次数上限触发安全策略
				if (PasswordRetryStrategy.DISABLE.equals(config.getPasswordRetryStrategy())) {
					user.disableUser();
				} else if (PasswordRetryStrategy.LOCK.equals(config.getPasswordRetryStrategy())) {
					LocalDateTime lockEndTime = LocalDateTime.now().plusSeconds(config.getPasswordRetryLockSeconds());
					user.lockUser(lockEndTime);
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public void onLoginSuccess(ISecurityUser user) {
		this.redisCache.deleteCacheMapValue(CACHE_KEY_PWD_RETRY, user.getType() + "_" + user.getUserId());
	}

	@Getter
	@Setter
	@NoArgsConstructor
	static class LoginPwdRetry {
		private String uid;
		private Integer num = 0;
		private LocalDate date = LocalDate.now();

		public LoginPwdRetry(String uid) {
			this.uid = uid;
		}

		public void inc() {
			LocalDate now = LocalDate.now();
			if (!now.isEqual(this.date)) {
				this.num = 0;
			}
			this.date = now;
			this.num++;
		}
	}
}
