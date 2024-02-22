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
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDictType;
import com.chestnut.system.fixed.FixedDictUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDictTypeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseRestController {
	
	private final ISysDictTypeService dictTypeService;

	@ExcelExportable(SysDictType.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping("/list")
	public R<?> list(SysDictType dictType) {
		PageRequest pr = this.getPageRequest();
		Page<SysDictType> page = dictTypeService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
				.like(StringUtils.isNotEmpty(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
				.orderByDesc(SysDictType::getDictType)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(dt -> {
			dt.setFixed(FixedDictUtils.isFixedDictType(dt.getDictType()) ? YesOrNo.YES : YesOrNo.NO);
		});
		I18nUtils.replaceI18nFields(page.getRecords(), LocaleContextHolder.getLocale());
		return bindDataTable(page);
	}

	/**
	 * 查询字典类型详细
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping(value = "/{dictId}")
	public R<?> getInfo(@PathVariable Long dictId) {
		SysDictType dictType = dictTypeService.getById(dictId);
		I18nUtils.replaceI18nFields(dictType);
		return R.ok(dictType);
	}

	/**
	 * 新增字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictAdd)
	@Log(title = "字典类型", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysDictType dict) {
		dict.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		dictTypeService.insertDictType(dict);
		return R.ok();
	}

	/**
	 * 修改字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictEdit)
	@Log(title = "字典类型", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysDictType dict) {
		dict.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		dictTypeService.updateDictType(dict);
		return R.ok();
	}

	/**
	 * 删除字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictRemove)
	@Log(title = "字典类型", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> dictIds) {
		dictTypeService.deleteDictTypeByIds(dictIds);
		return R.ok();
	}

	/**
	 * 刷新字典缓存
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictRemove)
	@Log(title = "字典类型", businessType = BusinessType.CLEAN)
	@DeleteMapping("/refreshCache")
	public R<?> refreshCache() {
		dictTypeService.resetDictCache();
		return R.ok();
	}

	/**
	 * 获取字典选择框列表
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/optionselect")
	public R<?> optionselect() {
		List<SysDictType> dictTypes = dictTypeService.list();
		I18nUtils.replaceI18nFields(dictTypes);
		return R.ok(dictTypes);
	}
}
