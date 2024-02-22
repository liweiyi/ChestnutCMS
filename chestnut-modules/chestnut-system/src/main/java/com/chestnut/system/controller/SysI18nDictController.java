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
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.I18nMessageSource;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.domain.SysI18nDict;
import com.chestnut.system.fixed.dict.I18nDictType;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysDictTypeService;
import com.chestnut.system.service.ISysI18nDictService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 国际化配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/i18n/dict")
public class SysI18nDictController extends BaseRestController {
	
	private final ISysI18nDictService i18nDictService;
	
	private final ISysDictTypeService dictTypeService;
	
	private final I18nMessageSource messageSource;

	@ExcelExportable(SysI18nDict.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictList)
	@GetMapping("/list")
	public R<?> list(SysI18nDict dict) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysI18nDict> q = new LambdaQueryWrapper<SysI18nDict>()
				.like(StringUtils.isNotEmpty(dict.getLangTag()), SysI18nDict::getLangTag, dict.getLangTag())
				.like(StringUtils.isNotEmpty(dict.getLangKey()), SysI18nDict::getLangKey, dict.getLangKey())
				.like(StringUtils.isNotEmpty(dict.getLangValue()), SysI18nDict::getLangValue, dict.getLangValue())
				.orderByDesc(SysI18nDict::getLangTag);
		Page<SysI18nDict> page = i18nDictService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@GetMapping("/langOptions")
	public R<?> bindLanguageOptions() {
		List<SysDictData> list = dictTypeService.selectDictDatasByType(I18nDictType.TYPE);
		I18nUtils.replaceI18nFields(list, LocaleContextHolder.getLocale());
		return R.ok(list.stream().map(d -> Map.of("value", d.getDictValue(), "label", d.getDictLabel())).toList());
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictList)
	@GetMapping(value = "/{i18nDictId}")
	public R<?> getInfo(@PathVariable Long i18nDictId) {
		return R.ok(this.i18nDictService.getById(i18nDictId));
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping(value = "/langKey/{langKey}")
	public R<?> listByLangKey(@PathVariable @NotEmpty String langKey) {
		LambdaQueryWrapper<SysI18nDict> q = new LambdaQueryWrapper<SysI18nDict>()
				.eq(SysI18nDict::getLangKey, langKey);
		List<SysI18nDict> list = i18nDictService.list(q);

		List<SysDictData> datas = dictTypeService.selectDictDatasByType(I18nDictType.TYPE);
		for (SysDictData data : datas) {
			Optional<SysI18nDict> opt = list.stream().filter(d -> d.getLangTag().equals(data.getDictValue())).findFirst();
			if (opt.isEmpty()) {
				SysI18nDict d = new SysI18nDict();
				d.setLangTag(data.getDictValue());
				d.setLangKey(langKey);
				d.setLangValue(I18nUtils.get("{" + langKey + "}", Locale.forLanguageTag(data.getDictValue())));
				list.add(d);
			}
		}
		return R.ok(list);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictAdd)
	@Log(title = "国际化管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysI18nDict config) {
		i18nDictService.insertI18nDict(config);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictEdit)
	@Log(title = "国际化管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysI18nDict dict) {
		i18nDictService.updateI18nDict(dict);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictEdit)
	@Log(title = "国际化管理", businessType = BusinessType.UPDATE)
	@PutMapping("/batch")
	public R<?> batchSave(@RequestBody @NotEmpty @Validated List<SysI18nDict> i18nDicts) {
		i18nDictService.batchSaveI18nDicts(i18nDicts);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictRemove)
	@Log(title = "国际化管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> i18nDictIds) {
		Assert.notEmpty(i18nDictIds, CommonErrorCode.INVALID_REQUEST_ARG::exception);
		i18nDictService.deleteI18nDictByIds(i18nDictIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictRemove)
	@Log(title = "国际化管理", businessType = BusinessType.CLEAN)
	@DeleteMapping("/refreshCache")
	public R<?> refreshCache() throws IOException {
		i18nDictService.loadMessages(this.messageSource);
		return R.ok();
	}
}
