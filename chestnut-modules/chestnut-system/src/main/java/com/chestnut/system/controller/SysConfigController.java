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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysConfig;
import com.chestnut.system.fixed.FixedConfigUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysConfigService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置 信息操作处理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseRestController {
	
	private final ISysConfigService configService;

	/**
	 * 获取参数配置列表
	 */
	@ExcelExportable(SysConfig.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigList)
	@GetMapping("/list")
	public R<?> list(SysConfig config) {
		PageRequest pr = this.getPageRequest();
		Page<SysConfig> page = this.configService.lambdaQuery()
				.like(StringUtils.isNotEmpty(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
				.like(StringUtils.isNotEmpty(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
				.orderByDesc(SysConfig::getConfigKey)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(conf -> {
			conf.setFixed(FixedConfigUtils.isFixedConfig(conf.getConfigKey()) ? YesOrNo.YES : YesOrNo.NO);
		});
		I18nUtils.replaceI18nFields(page.getRecords());
		return bindDataTable(page);
	}

	/**
	 * 根据参数编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigList)
	@GetMapping(value = "/{configId}")
	public R<?> getInfo(@PathVariable Long configId) {
		SysConfig config = this.configService.getById(configId);
		I18nUtils.replaceI18nFields(config);
		return R.ok(config);
	}

	/**
	 * 根据参数键名查询参数值
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping(value = "/configKey/{configKey}")
	public R<?> getConfigKey(@PathVariable String configKey) {
		return R.ok(configService.selectConfigByKey(configKey));
	}

	/**
	 * 新增参数配置
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigAdd)
	@Log(title = "参数管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysConfig config) {
		config.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		configService.insertConfig(config);
		return R.ok();
	}

	/**
	 * 修改参数配置
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigEdit)
	@Log(title = "参数管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysConfig config) {
		config.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		configService.updateConfig(config);
		return R.ok();
	}

	/**
	 * 删除参数配置
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigRemove)
	@Log(title = "参数管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> configIds) {
		Assert.isTrue(IdUtils.validate(configIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		configService.deleteConfigByIds(configIds);
		return R.ok();
	}

	/**
	 * 刷新参数缓存
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigRemove)
	@Log(title = "参数管理", businessType = BusinessType.CLEAN)
	@DeleteMapping("/refreshCache")
	public R<?> refreshCache() {
		configService.resetConfigCache();
		return R.ok();
	}
}
