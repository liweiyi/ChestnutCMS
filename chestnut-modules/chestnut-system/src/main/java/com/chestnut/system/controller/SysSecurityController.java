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
package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.CreateSecurityConfigRequest;
import com.chestnut.system.domain.dto.UpdateSecurityConfigRequest;
import com.chestnut.system.domain.vo.SecurityCheckVO;
import com.chestnut.system.fixed.dict.PasswordRule;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 安全配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/security/config")
public class SysSecurityController extends BaseRestController {

	private final ISecurityConfigService securityConfigService;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/list")
	public R<?> listConfigs() {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysSecurityConfig> q = new LambdaQueryWrapper<SysSecurityConfig>()
				.orderByDesc(SysSecurityConfig::getConfigId);
		Page<SysSecurityConfig> page = securityConfigService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/detail/{id}")
	public R<?> getConfig(@PathVariable @LongId Long id) {
		SysSecurityConfig securityConfig = securityConfigService.getById(id);
		Assert.notNull(securityConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(id));
		return R.ok(securityConfig);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/current")
	public R<?> getCurrentConfig() {
		SysSecurityConfig securityConfig = securityConfigService.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return R.ok();
		}
		String ruleRegex = PasswordRule.getRuleRegex(securityConfig.getPasswordRule());
		securityConfig.setPasswordRulePattern(ruleRegex);
		return R.ok(securityConfig);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<?> addConfig(@Validated @RequestBody CreateSecurityConfigRequest req) {
		this.securityConfigService.addConfig(req);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<?> saveConfig(@Validated @RequestBody UpdateSecurityConfigRequest req) {
		this.securityConfigService.saveConfig(req);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> delConfig(@RequestBody @NotEmpty List<Long> configIds) {
		this.securityConfigService.deleteConfigs(configIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.UPDATE)
	@PostMapping("/changeStatus/{id}")
	public R<?> changeConfigStatus(@PathVariable @LongId Long id) {
		this.securityConfigService.changeConfigStatus(id);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/check")
	public R<?> checkConfig() {
		SysSecurityConfig securityConfig = securityConfigService.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return R.ok();
		}

		SysUser user = (SysUser) StpAdminUtil.getLoginUser().getUser();
		SecurityCheckVO vo = new SecurityCheckVO();
		if (securityConfig.getPasswordExpireSeconds() > 0) {
			LocalDateTime lastModifyPwdTime = Objects.isNull(user.getPasswordModifyTime())
					? user.getCreateTime() : user.getPasswordModifyTime();
			if (lastModifyPwdTime.plusSeconds(securityConfig.getPasswordExpireSeconds()).isBefore(LocalDateTime.now())) {
				vo.setIsPasswordExpired(true);
			}
		}
		vo.setForceModifyPassword(user.checkForceModifyPassword());
		return R.ok(vo);
	}
}
