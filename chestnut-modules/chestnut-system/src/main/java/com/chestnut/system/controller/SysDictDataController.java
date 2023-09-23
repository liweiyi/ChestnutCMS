package com.chestnut.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.fixed.FixedDictUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDictDataService;
import com.chestnut.system.service.ISysDictTypeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseRestController {

	private final ISysDictDataService dictDataService;

	private final ISysDictTypeService dictTypeService;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping("/list")
	public R<?> list(SysDictData dictData) {
		PageRequest pr = this.getPageRequest();
		Page<SysDictData> page = dictDataService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictData.getDictLabel()), SysDictData::getDictLabel,
						dictData.getDictLabel())
				.eq(StringUtils.isNotEmpty(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
				.orderByAsc(SysDictData::getDictSort)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(dt -> {
			dt.setFixed(FixedDictUtils.isFixedDictData(dt.getDictType(), dt.getDictValue()) ? YesOrNo.YES : YesOrNo.NO);
		});
		I18nUtils.replaceI18nFields(page.getRecords(), LocaleContextHolder.getLocale());
		return bindDataTable(page);
	}

	@Log(title = "字典数据", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysDictData dictData) {
		List<SysDictData> list = dictDataService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictData.getDictLabel()), SysDictData::getDictLabel,
						dictData.getDictLabel())
				.like(StringUtils.isNotEmpty(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
				.orderByDesc(SysDictData::getDictSort).list();
		this.exportExcel(list, SysDictData.class, response);
	}

	/**
	 * 查询字典数据详细
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping(value = "/{dictCode}")
	public R<?> getInfo(@PathVariable Long dictCode) {
		SysDictData data = dictDataService.getById(dictCode);
		I18nUtils.replaceI18nFields(data);
		return R.ok(data);
	}

	/**
	 * 根据字典类型查询字典数据信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping(value = "/type/{dictType}")
	public R<?> getDictDatasByType(@PathVariable String dictType) {
		List<SysDictData> datas = dictTypeService.selectDictDatasByType(dictType);
		I18nUtils.replaceI18nFields(datas);
		return R.ok(datas);
	}

	/**
	 * 新增字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictAdd)
	@Log(title = "字典数据", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysDictData dict) {
		dict.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		dictDataService.insertDictData(dict);
		return R.ok();
	}

	/**
	 * 修改保存字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictEdit)
	@Log(title = "字典数据", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysDictData dict) {
		dict.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		dictDataService.updateDictData(dict);
		return R.ok();
	}

	/**
	 * 删除字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictRemove)
	@Log(title = "字典类型", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> dictCodes) {
		dictDataService.deleteDictDataByIds(dictCodes);
		return R.ok();
	}
}
