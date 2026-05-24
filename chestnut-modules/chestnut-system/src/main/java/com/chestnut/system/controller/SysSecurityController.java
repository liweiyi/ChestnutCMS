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
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.dto.CreateSecurityConfigRequest;
import com.chestnut.system.domain.dto.UpdateSecurityConfigRequest;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 安全配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.SECURITY.MODULE}")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/security/config")
public class SysSecurityController extends BaseRestController {

	private final ISecurityConfigService securityConfigService;

	@XComment("{API.DOC.SYS.SECURITY.GET_LIST}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/list")
	public R<TableData<SysSecurityConfig>> listConfigs() {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysSecurityConfig> q = new LambdaQueryWrapper<SysSecurityConfig>()
				.orderByDesc(SysSecurityConfig::getConfigId);
		Page<SysSecurityConfig> page = securityConfigService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@XComment("{API.DOC.SYS.SECURITY.GET_INFO}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/detail/{id}")
	public R<SysSecurityConfig> getConfig(@PathVariable @LongId @XComment("{API.DOC.SYS.SECURITY.ID}") Long id) {
		SysSecurityConfig config = securityConfigService.getById(id);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(id));
		securityConfigService.fixeOldVersion(config);
		return R.ok(config);
	}

	@XComment("{API.DOC.SYS.SECURITY.GET_CONFIGS}")
	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/get")
	public R<?> getConfigs(@RequestBody List<String> types) {
		SysSecurityConfig securityConfig = securityConfigService.getSecurityConfig();
		if (Objects.isNull(securityConfig)) {
			return R.ok(JacksonUtils.objectNode());
		}
		if (StringUtils.isEmpty(types)) {
			return R.ok(securityConfig.getConfigs());
		}
		Map<String, Object> configs = securityConfigService.getSecurityConfigs(types);
		return R.ok(configs);
	}

	@XComment("{API.DOC.SYS.SECURITY.CREATE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> addConfig(@Validated @RequestBody CreateSecurityConfigRequest req) {
		this.securityConfigService.addConfig(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.SECURITY.UPDATE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> saveConfig(@Validated @RequestBody UpdateSecurityConfigRequest req) {
		this.securityConfigService.saveConfig(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.SECURITY.DELETE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> delConfig(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.SECURITY.IDS}") List<Long> configIds) {
		this.securityConfigService.deleteConfigs(configIds);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.SECURITY.CHANGE_STATUS}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "安全配置", businessType = BusinessType.UPDATE)
	@PostMapping("/changeStatus/{id}")
	public R<Void> changeConfigStatus(@PathVariable @LongId @XComment("{API.DOC.SYS.SECURITY.ID}") Long id) {
		this.securityConfigService.changeConfigStatus(id);
		return R.ok();
	}
}
