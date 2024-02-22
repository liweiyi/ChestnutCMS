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
import com.chestnut.system.domain.SysWeChatConfig;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysWeChatConfigService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信参数配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/wechat")
public class SysWeChatController extends BaseRestController {

	private final ISysWeChatConfigService weChatConfigService;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysWeChatConfigView)
	@GetMapping
	public R<?> listConfigs() {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysWeChatConfig> q = new LambdaQueryWrapper<SysWeChatConfig>()
				.orderByDesc(SysWeChatConfig::getConfigId);
		Page<SysWeChatConfig> page = weChatConfigService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysWeChatConfigView)
	@GetMapping("/{id}")
	public R<?> getConfig(@PathVariable Long id) {
		SysWeChatConfig config = weChatConfigService.getById(id);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(id));
		return R.ok(config);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysWeChatConfigView)
	@Log(title = "微信配置", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addConfig(@Validated @RequestBody SysWeChatConfig config) {
		config.setOperator(StpAdminUtil.getLoginUser());
		this.weChatConfigService.addWeChatConfig(config);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysWeChatConfigView)
	@Log(title = "微信配置", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> saveConfig(@Validated @RequestBody SysWeChatConfig config) {
		config.setOperator(StpAdminUtil.getLoginUser());
		this.weChatConfigService.editWeChatConfig(config);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysWeChatConfigView)
	@Log(title = "微信配置", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> delConfig(@RequestBody @NotEmpty List<Long> configIds) {
		this.weChatConfigService.deleteWeChatConfigs(configIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysWeChatConfigView)
	@Log(title = "微信配置", businessType = BusinessType.UPDATE)
	@PutMapping("/change_status/{id}")
	public R<?> changeConfigStatus(@PathVariable @LongId Long id) {
		this.weChatConfigService.changeConfigStatus(id);
		return R.ok();
	}
}
