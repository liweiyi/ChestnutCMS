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
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.dto.CreateLoginConfigRequest;
import com.chestnut.system.domain.dto.UpdateLoginConfigRequest;
import com.chestnut.system.security.ILoginType;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ILoginConfigService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 三方登录配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.LOGIN_CONFIG.MODULE}")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/login/config")
public class SysLoginConfigController extends BaseRestController {

	private final ILoginConfigService loginConfigService;

    private final List<ILoginType>  loginTypeList;

	@Priv(type = AdminUserType.TYPE)
    @XComment("{API.DOC.SYS.LOGIN_CONFIG.GET_TYPE_OPTIONS}")
    @GetMapping("/typeOptions")
    public R<?> getLoginTypeOptions() {
        return bindSelectOptions(loginTypeList, ILoginType::getType, ILoginType::getName);
    }

    @XComment("{API.DOC.SYS.LOGIN_CONFIG.GET_OPTIONS}")
    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
    @GetMapping("/options")
    public R<?> getConfigOptions() {
        List<SysLoginConfig> list = this.loginConfigService.list();
        return bindSelectOptions(list, config -> config.getConfigId().toString(), SysLoginConfig::getConfigName);
    }

    @XComment("{API.DOC.SYS.LOGIN_CONFIG.GET_LIST}")
    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/list")
	public R<TableData<SysLoginConfig>> listConfigs() {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysLoginConfig> q = new LambdaQueryWrapper<SysLoginConfig>()
				.orderByDesc(SysLoginConfig::getConfigId);
		Page<SysLoginConfig> page = loginConfigService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
        page.getRecords().forEach(c -> c.setConfigProps(null));
		return bindDataTable(page);
	}

    @XComment("{API.DOC.SYS.LOGIN_CONFIG.GET_INFO}")
    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/detail")
	public R<SysLoginConfig> getConfig(@RequestParam @LongId @XComment("{API.DOC.SYS.LOGIN_CONFIG.ID}") Long configId) {
        SysLoginConfig config = loginConfigService.getById(configId);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));
        ILoginType loginType = this.loginConfigService.getLoginType(config.getType());
        loginType.dealSensitive(config.getConfigProps());
        return R.ok(config);
	}

    @XComment("{API.DOC.SYS.LOGIN_CONFIG.CREATE}")
    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "登录配置", businessType = BusinessType.INSERT, isSaveRequestData = false)
	@PostMapping("/add")
	public R<Void> addConfig(@Validated @RequestBody CreateLoginConfigRequest req) {
		this.loginConfigService.addConfig(req);
		return R.ok();
	}

    @XComment("{API.DOC.SYS.LOGIN_CONFIG.UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "登录配置", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PostMapping("/update")
	public R<Void> saveConfig(@Validated @RequestBody UpdateLoginConfigRequest req) {
		this.loginConfigService.saveConfig(req);
		return R.ok();
	}

    @XComment("{API.DOC.SYS.LOGIN_CONFIG.DELETE}")
    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "登录配置", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> delConfig(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.LOGIN_CONFIG.IDS}") List<Long> configIds) {
		this.loginConfigService.deleteConfigs(configIds);
		return R.ok();
	}
}
