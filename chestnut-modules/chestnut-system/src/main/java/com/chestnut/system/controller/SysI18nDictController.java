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
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;


import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.I18nMessageSource;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.domain.SysI18nDict;
import com.chestnut.system.domain.dto.BatchSaveI18nDictRequest;
import com.chestnut.system.domain.dto.CreateI18nDictRequest;
import com.chestnut.system.domain.dto.QueryI18nDictRequest;
import com.chestnut.system.domain.dto.UpdateI18nDictRequest;
import com.chestnut.system.fixed.dict.I18nDictType;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysDictTypeService;
import com.chestnut.system.service.ISysI18nDictService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 国际化配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.I18N_DICT.MODULE}")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/i18n/dict")
public class SysI18nDictController extends BaseRestController {
	
	private final ISysI18nDictService i18nDictService;
	
	private final ISysDictTypeService dictTypeService;
	
	private final I18nMessageSource messageSource;

	@XComment("{API.DOC.SYS.I18N_DICT.GET_LIST}")
	@ExcelExportable(SysI18nDict.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictList)
	@GetMapping("/list")
	public R<TableData<SysI18nDict>> list(@Validated QueryI18nDictRequest req) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysI18nDict> q = new LambdaQueryWrapper<SysI18nDict>()
				.like(StringUtils.isNotEmpty(req.getLangTag()), SysI18nDict::getLangTag, req.getLangTag())
				.like(StringUtils.isNotEmpty(req.getLangKey()), SysI18nDict::getLangKey, req.getLangKey())
				.like(StringUtils.isNotEmpty(req.getLangValue()), SysI18nDict::getLangValue, req.getLangValue())
				.orderByDesc(SysI18nDict::getLangTag);
		Page<SysI18nDict> page = i18nDictService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}
	@XComment("{API.DOC.SYS.I18N_DICT.GET_LANG_OPTIONS}")
	@GetMapping("/langOptions")
	public R<?> bindLanguageOptions() {
		List<SysDictData> list = dictTypeService.selectDictDatasByType(I18nDictType.TYPE);
		I18nUtils.replaceI18nFields(list, LocaleContextHolder.getLocale());
		return bindSelectOptions(list, SysDictData::getDictValue, SysDictData::getDictLabel);
	}

	@XComment("{API.DOC.SYS.I18N_DICT.GET_INFO}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictList)
	@GetMapping(value = "/detail/{i18nDictId}")
	public R<SysI18nDict> getInfo(@PathVariable @LongId @XComment("{API.DOC.SYS.I18N_DICT.ID}") Long i18nDictId) {
		return R.ok(this.i18nDictService.getById(i18nDictId));
	}

	@XComment("{API.DOC.SYS.I18N_DICT.GET_BY_KEY}")
	@Priv(type = AdminUserType.TYPE)
	@GetMapping(value = "/langKey/{langKey}")
	public R<List<SysI18nDict>> listByLangKey(@PathVariable @NotBlank @XComment("{API.DOC.SYS.I18N_DICT.LANG_KEY}") String langKey) {
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
				d.setLangValue(I18nUtils.parse(langKey, Locale.forLanguageTag(data.getDictValue())));
				list.add(d);
			}
		}
		return R.ok(list);
	}

	@XComment("{API.DOC.SYS.I18N_DICT.CREATE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictAdd)
	@Log(title = "国际化管理", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@Validated @RequestBody CreateI18nDictRequest req) {
		i18nDictService.insertI18nDict(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.I18N_DICT.UPDATE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictEdit)
	@Log(title = "国际化管理", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> edit(@Validated @RequestBody UpdateI18nDictRequest req) {
		i18nDictService.updateI18nDict(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.I18N_DICT.BATCH_SAVE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictEdit)
	@Log(title = "国际化管理", businessType = BusinessType.UPDATE)
	@PostMapping("/batch")
	public R<Void> batchSave(@RequestBody @NotEmpty @Validated List<BatchSaveI18nDictRequest> i18nDicts) {
		i18nDictService.batchSaveI18nDicts(i18nDicts);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.I18N_DICT.DELETE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictRemove)
	@Log(title = "国际化管理", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.I18N_DICT.IDS}") List<Long> i18nDictIds) {
		Assert.notEmpty(i18nDictIds, CommonErrorCode.INVALID_REQUEST_ARG::exception);
		i18nDictService.deleteI18nDictByIds(i18nDictIds);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.I18N_DICT.REFRESH_CACHE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictRemove)
	@Log(title = "国际化管理", businessType = BusinessType.CLEAN)
	@PostMapping("/refreshCache")
	public R<Void> refreshCache() throws IOException {
		i18nDictService.loadMessages(this.messageSource);
		return R.ok();
	}
}
