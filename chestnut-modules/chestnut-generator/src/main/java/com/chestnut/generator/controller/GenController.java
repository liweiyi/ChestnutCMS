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
package com.chestnut.generator.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.generator.domain.GenTable;
import com.chestnut.generator.domain.GenTableColumn;
import com.chestnut.generator.permission.GenMenuPriv;
import com.chestnut.generator.service.IGenTableColumnService;
import com.chestnut.generator.service.IGenTableService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 代码生成 操作处理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
public class GenController extends BaseRestController {
	
	private final IGenTableService genTableService;

	private final IGenTableColumnService genTableColumnService;

	/**
	 * 查询代码生成列表
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.List)
	@GetMapping("/list")
	public R<?> genList(GenTable genTable) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<GenTable> q =  new LambdaQueryWrapper<GenTable>()
			.like(StringUtils.isNotEmpty(genTable.getTableName()), GenTable::getTableName, genTable.getTableName())
			.like(StringUtils.isNotEmpty(genTable.getTableComment()), GenTable::getTableComment, genTable.getTableComment())
			.ge(Objects.nonNull(genTable.getParams().get("beginTime")), GenTable::getCreateTime, genTable.getParams().get("beginTime"))
			.le(Objects.nonNull(genTable.getParams().get("endTime")), GenTable::getCreateTime, genTable.getParams().get("endTime"));
		Page<GenTable> page = genTableService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	/**
	 * 修改代码生成业务
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.List)
	@GetMapping(value = "/{tableId}")
	public R<?> getInfo(@PathVariable Long tableId) {
		GenTable table = genTableService.selectGenTableById(tableId);
		List<GenTable> tables = genTableService.selectGenTableAll();
		List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("info", table);
		map.put("rows", list);
		map.put("tables", tables);
		return R.ok(map);
	}

	/**
	 * 查询数据库列表
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.List)
	@GetMapping("/db/list")
	public R<?> dataList(GenTable genTable) {
		List<GenTable> list = genTableService.selectDbTableList(genTable);
		return bindDataTable(list);
	}

	/**
	 * 查询数据表字段列表
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.List)
	@GetMapping(value = "/column/{tableId}")
	public R<?> columnList(Long tableId) {
		List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
		return this.bindDataTable(list);
	}

	/**
	 * 导入表结构（保存）
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.Import)
	@Log(title = "代码生成", businessType = BusinessType.IMPORT)
	@PostMapping("/importTable")
	public R<?> importTableSave(String tables) {
		String[] tableNames = StringUtils.split(tables, StringUtils.C_COMMA);
		// 查询表信息
		List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
		genTableService.importGenTable(tableList, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 修改保存代码生成业务
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.Edit)
	@Log(title = "代码生成", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editSave(@Validated @RequestBody GenTable genTable) {
		genTableService.validateEdit(genTable);
		genTableService.updateGenTable(genTable);
		return R.ok();
	}

	/**
	 * 删除代码生成
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.Remove)
	@Log(title = "代码生成", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> tableIds) {
		this.genTableService.deleteGenTableByIds(tableIds);
		return R.ok();
	}

	/**
	 * 预览代码
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.Preview)
	@GetMapping("/preview/{tableId}")
	public R<?> preview(@PathVariable("tableId") Long tableId) throws IOException {
		Map<String, String> dataMap = genTableService.previewCode(tableId);
		return R.ok(dataMap);
	}

	/**
	 * 生成代码（下载方式）
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.GenCode)
	@Log(title = "代码生成", businessType = BusinessType.GENCODE)
	@GetMapping("/download/{tableName}")
	public void download(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
		byte[] data = genTableService.downloadCode(tableName);
		genCode(response, data);
	}

	/**
	 * 生成代码（自定义路径）
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.GenCode)
	@Log(title = "代码生成", businessType = BusinessType.GENCODE)
	@GetMapping("/genCode/{tableName}")
	public R<?> genCode(@PathVariable("tableName") String tableName) {
		genTableService.generatorCode(tableName);
		return R.ok();
	}

	/**
	 * 同步数据库
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.Edit)
	@Log(title = "代码生成", businessType = BusinessType.UPDATE)
	@GetMapping("/synchDb/{tableName}")
	public R<?> synchDb(@PathVariable("tableName") String tableName) {
		genTableService.synchDb(tableName);
		return R.ok();
	}

	/**
	 * 批量生成代码
	 */
	@Priv(type = AdminUserType.TYPE, value = GenMenuPriv.GenCode)
	@Log(title = "代码生成", businessType = BusinessType.GENCODE)
	@GetMapping("/batchGenCode")
	public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
		String[] tableNames = StringUtils.split(tables, StringUtils.C_COMMA);
		byte[] data = genTableService.downloadCode(tableNames);
		genCode(response, data);
	}

	/**
	 * 生成zip文件
	 */
	private void genCode(HttpServletResponse response, byte[] data) throws IOException {
		response.reset();
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
		response.setHeader("Content-Disposition", "attachment; filename=\"chestnut.zip\"");
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");
		IOUtils.write(data, response.getOutputStream());
	}
}