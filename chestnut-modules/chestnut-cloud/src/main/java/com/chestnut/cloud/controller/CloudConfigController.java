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
package com.chestnut.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cloud.domain.CcCloudConfig;
import com.chestnut.cloud.domain.dto.CreateCloudConfigRequest;
import com.chestnut.cloud.domain.dto.UpdateCloudConfigRequest;
import com.chestnut.cloud.service.ICloudConfigService;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 云服务配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/cloud/config")
public class CloudConfigController extends BaseRestController {

	private final ICloudConfigService cloudConfigService;

    private final List<ICloudProvider> cloudProviderList;

    @GetMapping("/typeOptions")
    public R<?> getLoginTypeOptions() {
        return bindSelectOptions(cloudProviderList, ICloudProvider::getId, ICloudProvider::getName);
    }

    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/list")
	public R<?> listConfigs() {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<CcCloudConfig> q = new LambdaQueryWrapper<CcCloudConfig>()
				.orderByDesc(CcCloudConfig::getConfigId);
		Page<CcCloudConfig> page = cloudConfigService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
        page.getRecords().forEach(c -> c.setConfigProps(null));
		return bindDataTable(page);
	}

    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/options")
    public R<?> selectOptions() {
        List<CcCloudConfig> list = cloudConfigService.lambdaQuery().list();
        return bindSelectOptions(list, config -> config.getConfigId().toString(), CcCloudConfig::getConfigName);
    }

    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@GetMapping("/detail/{configId}")
	public R<?> getConfig(@PathVariable @LongId Long configId) {
        CcCloudConfig config = cloudConfigService.getById(configId);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));
        ICloudProvider cloudProvider = this.cloudConfigService.getCloudProvider(config.getType());
        cloudProvider.dealSensitive(config.getConfigProps());
        return R.ok(config);
	}

    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "云服务配置", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<?> addConfig(@Validated @RequestBody CreateCloudConfigRequest req) {
		this.cloudConfigService.addConfig(req);
		return R.ok();
	}

    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "云服务配置", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<?> saveConfig(@Validated @RequestBody UpdateCloudConfigRequest req) {
		this.cloudConfigService.saveConfig(req);
		return R.ok();
	}

    @Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysSecurityList)
	@Log(title = "云服务配置", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> delConfig(@RequestBody @NotEmpty List<Long> configIds) {
		this.cloudConfigService.deleteConfigs(configIds);
		return R.ok();
	}
}
