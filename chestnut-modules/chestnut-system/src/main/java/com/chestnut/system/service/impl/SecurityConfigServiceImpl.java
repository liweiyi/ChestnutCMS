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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.captcha.CaptchaService;
import com.chestnut.common.captcha.ICaptchaType;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.dto.CreateSecurityConfigRequest;
import com.chestnut.system.domain.dto.LoginBody;
import com.chestnut.system.domain.dto.UpdateSecurityConfigRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.mapper.SysSecurityConfigMapper;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.ISecurityUser;
import com.chestnut.system.security.config.*;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysLogininforService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SecurityConfigServiceImpl extends ServiceImpl<SysSecurityConfigMapper, SysSecurityConfig>
		implements ISecurityConfigService {

	private final static String CACHE_KEY_CONFIG = "sys:security:config";

	private final RedisCache redisCache;

	private final List<ISecurityConfigType<?>> securityConfigTypes;

	private final PasswordSecurityConfigType passwordSecurityConfigType;

	private final LoginSecurityConfigType loginSecurityConfigType;

	private final CaptchaService captchaService;

	private final ISysLogininforService logininfoService;
	
	@Override
	public SysSecurityConfig getSecurityConfig() {
		SysSecurityConfig config = this.redisCache.getCacheObject(CACHE_KEY_CONFIG, SysSecurityConfig.class, () -> {
				return lambdaQuery().eq(SysSecurityConfig::getStatus, EnableOrDisable.ENABLE).one();
			}
		);
		this.fixeOldVersion(config);
		return config;
	}

	@Override
	public void fixeOldVersion(SysSecurityConfig config) {
		if (Objects.nonNull(config) && config.getConfigs().isEmpty()) {
			// 兼容历史数据
			boolean update = false;
			for (ISecurityConfigType<?> sct : securityConfigTypes) {
				if (!config.getConfigs().has(sct.getType())) {
					sct.fixedOldVersion(config);
					update = true;
				}
			}
			if (update) {
				this.updateById(config);
			}
		}
	}

	@Override
	public Map<String, Object> getSecurityConfigs(List<String> configTypes) {
		Map<String, Object> configs = new HashMap<>();
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		this.securityConfigTypes.forEach(ict -> {
			if (configTypes.contains(ict.getType())) {
				Object config = ict.getConfig(securityConfig.getConfigs());
				configs.put(ict.getType(), config);
			}
		});
		return configs;
	}

	@Override
	public void addConfig(CreateSecurityConfigRequest req) {
		SysSecurityConfig config = new SysSecurityConfig();
		BeanUtils.copyProperties(req, config);
		config.setConfigId(IdUtils.getSnowflakeId());
		config.setStatus(EnableOrDisable.DISABLE); // 默认不开启
		config.createBy(req.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void saveConfig(UpdateSecurityConfigRequest req) {
		SysSecurityConfig dbConfig = this.getById(req.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getConfigId()));

		BeanUtils.copyProperties(req, dbConfig);
		dbConfig.updateBy(req.getOperator().getUsername());
		this.updateById(dbConfig);
		clearConfigCache();
	}

	@Override
	public void deleteConfigs(List<Long> configIds) {
		this.removeByIds(configIds);
		clearConfigCache();
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
		clearConfigCache();
	}

	@Override
	public void validPassword(ISecurityUser user, String password) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return;
		}
		PasswordSecurity passwordSecurity = passwordSecurityConfigType.getConfig(securityConfig.getConfigs());
		passwordSecurity.validPassword(user, password);
	}

	@Override
	public void forceModifyPwdAfterResetPwd(ISecurityUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return;
		}
		PasswordSecurity passwordSecurity = passwordSecurityConfigType.getConfig(securityConfig.getConfigs());
		passwordSecurity.checkForceModifyPwdAfterReset(user);
	}

	@Override
	public void forceModifyPwdAfterUserAdd(ISecurityUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return;
		}
		PasswordSecurity passwordSecurity = passwordSecurityConfigType.getConfig(securityConfig.getConfigs());
		passwordSecurity.checkForceModifyPwdAfterAdd(user);
	}

	/**
	 * 
	 */
	@Override
	public void processLoginPasswordError(ISecurityUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return;
		}
		LoginSecurity loginSecurity = loginSecurityConfigType.getConfig(securityConfig.getConfigs());
		loginSecurityConfigType.processLoginPasswordError(loginSecurity, user);
	}

	@Override
	public void onLoginSuccess(ISecurityUser user) {
		loginSecurityConfigType.onLoginSuccess(user);
	}

	public void validateLoginCaptcha(LoginBody loginBody) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.isNull(securityConfig) || !securityConfig.isEnable()) {
			return;
		}
		LoginSecurity config = loginSecurityConfigType.getConfig(securityConfig.getConfigs());
		if (Objects.nonNull(config) && !YesOrNo.isYes(config.getCaptchaEnable())) {
			return;
		}
		Assert.notNull(loginBody.getCaptcha(), SysErrorCode.CAPTCHA_ERR::exception);

		ICaptchaType captchaType = captchaService.getCaptchaType(config.getCaptchaType());
		boolean validated = captchaType.isTokenValidated(loginBody.getCaptcha());
		if (!validated) {
			this.logininfoService.recordLogininfor(AdminUserType.TYPE, null, loginBody.getUsername(),
					LoginLogType.LOGIN, SuccessOrFail.FAIL, SysErrorCode.CAPTCHA_ERR.name());
			throw SysErrorCode.CAPTCHA_ERR.exception();
		}
	}

	private void clearConfigCache() {
		this.redisCache.deleteObject(CACHE_KEY_CONFIG);
		this.redisCache.setCacheObject(ISecurityConfigService.CACHE_KEY_CONFIG_LAST_MODIFIED,
				System.currentTimeMillis());
	}
}
