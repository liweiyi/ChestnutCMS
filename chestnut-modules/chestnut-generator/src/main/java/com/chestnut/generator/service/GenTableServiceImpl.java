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
package com.chestnut.generator.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.generator.GenConstants;
import com.chestnut.generator.domain.GenTable;
import com.chestnut.generator.domain.GenTableColumn;
import com.chestnut.generator.exception.GenErrorCode;
import com.chestnut.generator.mapper.GenTableColumnMapper;
import com.chestnut.generator.mapper.GenTableMapper;
import com.chestnut.generator.util.GenUtils;
import com.chestnut.generator.util.VelocityInitializer;
import com.chestnut.generator.util.VelocityUtils;

/**
 * 业务 服务层实现
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
public class GenTableServiceImpl extends ServiceImpl<GenTableMapper, GenTable> implements IGenTableService {

	private static final Logger log = LoggerFactory.getLogger(GenTableServiceImpl.class);

	@Autowired
	private GenTableMapper genTableMapper;

	@Autowired
	private GenTableColumnMapper genTableColumnMapper;

	/**
	 * 查询业务信息
	 * 
	 * @param id
	 *            业务ID
	 * @return 业务信息
	 */
	@Override
	public GenTable selectGenTableById(Long id) {
		GenTable genTable = genTableMapper.selectGenTableById(id);
		setTableFromOptions(genTable);
		return genTable;
	}

	/**
	 * 查询据库列表
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 数据库表集合
	 */
	@Override
	public List<GenTable> selectDbTableList(GenTable genTable) {
		return genTableMapper.selectDbTableList(genTable.getTableName(), genTable.getTableComment());
	}

	/**
	 * 查询据库列表
	 * 
	 * @param tableNames
	 *            表名称组
	 * @return 数据库表集合
	 */
	@Override
	public List<GenTable> selectDbTableListByNames(String[] tableNames) {
		return genTableMapper.selectDbTableListByNames(tableNames);
	}

	/**
	 * 查询所有表信息
	 * 
	 * @return 表信息集合
	 */
	@Override
	public List<GenTable> selectGenTableAll() {
		return genTableMapper.selectGenTableAll();
	}

	/**
	 * 修改业务
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateGenTable(GenTable genTable) {
		String options = JacksonUtils.to(genTable.getParams());
		genTable.setOptions(options);
		int row = genTableMapper.updateGenTable(genTable);
		if (row > 0) {
			for (GenTableColumn cenTableColumn : genTable.getColumns()) {
				genTableColumnMapper.updateGenTableColumn(cenTableColumn);
			}
		}
	}

	/**
	 * 删除业务对象
	 * 
	 * @param tableIds
	 *            需要删除的数据ID
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteGenTableByIds(List<Long> tableIds) {
		genTableMapper.delete(new LambdaQueryWrapper<GenTable>().in(GenTable::getTableId, tableIds));
		genTableColumnMapper.delete(new LambdaQueryWrapper<GenTableColumn>().in(GenTableColumn::getTableId, tableIds));
	}

	/**
	 * 导入表结构
	 * 
	 * @param tableList
	 *            导入表列表
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importGenTable(List<GenTable> tableList, String operator) {
		try {
			for (GenTable table : tableList) {
				String tableName = table.getTableName();
				GenUtils.initTable(table, operator);
				int row = genTableMapper.insertGenTable(table);
				if (row > 0) {
					// 保存列信息
					List<GenTableColumn> genTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
					for (GenTableColumn column : genTableColumns) {
						GenUtils.initColumnField(column, table);
						genTableColumnMapper.insertGenTableColumn(column);
					}
				}
			}
		} catch (Exception e) {
			throw GenErrorCode.IMPORT_ERR.exception(e.getMessage());
		}
	}

	/**
	 * 预览代码
	 * 
	 * @param tableId
	 *            表编号
	 * @return 预览数据列表
	 */
	@Override
	public Map<String, String> previewCode(Long tableId) {
		Map<String, String> dataMap = new LinkedHashMap<>();
		// 查询表信息
		GenTable table = genTableMapper.selectGenTableById(tableId);
		// 设置主子表信息
		setSubTable(table);
		// 设置主键列信息
		setPkColumn(table);
		VelocityInitializer.initVelocity();

		VelocityContext context = VelocityUtils.prepareContext(table);

		// 获取模板列表
		List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
		for (String template : templates) {
			// 渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
			tpl.merge(context, sw);
			dataMap.put(template, sw.toString());
		}
		return dataMap;
	}

	/**
	 * 生成代码（下载方式）
	 * 
	 * @param tableName
	 *            表名称
	 * @return 数据
	 */
	@Override
	public byte[] downloadCode(String tableName) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		generatorCode(tableName, zip);
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}

	/**
	 * 生成代码（自定义路径）
	 * 
	 * @param tableName
	 *            表名称
	 */
	@Override
	public void generatorCode(String tableName) {
		// 查询表信息
		GenTable table = genTableMapper.selectGenTableByName(tableName);
		// 设置主子表信息
		setSubTable(table);
		// 设置主键列信息
		setPkColumn(table);

		VelocityInitializer.initVelocity();

		VelocityContext context = VelocityUtils.prepareContext(table);

		// 获取模板列表
		List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
		for (String template : templates) {
			if (!StringUtils.containsAny(template, "sql.vm", "api.js.vm", "index.vue.vm", "index-tree.vue.vm")) {
				// 渲染模板
				StringWriter sw = new StringWriter();
				Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
				tpl.merge(context, sw);
				try {
					String path = getGenPath(table, template);
					FileUtils.writeStringToFile(new File(path), sw.toString(), StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw GenErrorCode.TEMPLATE_ERR.exception(table.getTableName());
				}
			}
		}
	}

	/**
	 * 同步数据库
	 * 
	 * @param tableName
	 *            表名称
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void synchDb(String tableName) {
		GenTable table = genTableMapper.selectGenTableByName(tableName);
		List<GenTableColumn> tableColumns = table.getColumns();
		Map<String, GenTableColumn> tableColumnMap = tableColumns.stream()
				.collect(Collectors.toMap(GenTableColumn::getColumnName, Function.identity()));

		List<GenTableColumn> dbTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
		if (StringUtils.isEmpty(dbTableColumns)) {
			throw GenErrorCode.SOURCE_TABLE_NOT_EXISTS.exception();
		}
		List<String> dbTableColumnNames = dbTableColumns.stream().map(GenTableColumn::getColumnName)
				.collect(Collectors.toList());

		dbTableColumns.forEach(column -> {
			GenUtils.initColumnField(column, table);
			if (tableColumnMap.containsKey(column.getColumnName())) {
				GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
				column.setColumnId(prevColumn.getColumnId());
				if (column.isList()) {
					// 如果是列表，继续保留查询方式/字典类型选项
					column.setDictType(prevColumn.getDictType());
					column.setQueryType(prevColumn.getQueryType());
				}
				if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
						&& (column.isInsert() || column.isEdit())
						&& ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
					// 如果是(新增/修改&非主键/非忽略及父属性)，继续保留必填/显示类型选项
					column.setIsRequired(prevColumn.getIsRequired());
					column.setHtmlType(prevColumn.getHtmlType());
				}
				genTableColumnMapper.updateGenTableColumn(column);
			} else {
				genTableColumnMapper.insertGenTableColumn(column);
			}
		});

		List<GenTableColumn> delColumns = tableColumns.stream()
				.filter(column -> !dbTableColumnNames.contains(column.getColumnName())).toList();
		this.removeBatchByIds(delColumns);
	}

	/**
	 * 批量生成代码（下载方式）
	 * 
	 * @param tableNames
	 *            表数组
	 * @return 数据
	 */
	@Override
	public byte[] downloadCode(String[] tableNames) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		for (String tableName : tableNames) {
			generatorCode(tableName, zip);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}

	/**
	 * 查询表信息并生成代码
	 */
	private void generatorCode(String tableName, ZipOutputStream zip) {
		// 查询表信息
		GenTable table = genTableMapper.selectGenTableByName(tableName);
		// 设置主子表信息
		setSubTable(table);
		// 设置主键列信息
		setPkColumn(table);

		VelocityInitializer.initVelocity();

		VelocityContext context = VelocityUtils.prepareContext(table);

		// 获取模板列表
		List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
		for (String template : templates) {
			// 渲染模板
			StringWriter sw = new StringWriter();
			Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
			tpl.merge(context, sw);
			try {
				// 添加到zip
				zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
				IOUtils.write(sw.toString(), zip, StandardCharsets.UTF_8.name());
				IOUtils.closeQuietly(sw);
				zip.flush();
				zip.closeEntry();
			} catch (IOException e) {
				log.error("渲染模板失败，表名：" + table.getTableName(), e);
			}
		}
	}

	/**
	 * 修改保存参数校验
	 * 
	 * @param genTable
	 *            业务信息
	 */
	@Override
	public void validateEdit(GenTable genTable) {
		if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
			if (StringUtils.isEmpty(MapUtils.getString(genTable.getParams(), GenConstants.TREE_CODE))) {
				throw GenErrorCode.TREE_CODE_EMPTY.exception();
			} else if (StringUtils.isEmpty(MapUtils.getString(genTable.getParams(), GenConstants.TREE_PARENT_CODE))) {
				throw GenErrorCode.PARENT_TREE_CODE_EMPTY.exception();
			} else if (StringUtils.isEmpty(MapUtils.getString(genTable.getParams(), GenConstants.TREE_NAME))) {
				throw GenErrorCode.TREE_NAME_EMPTY.exception();
			} else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory())) {
				if (StringUtils.isEmpty(genTable.getSubTableName())) {
					throw GenErrorCode.SUBTABLE_NAME_EMPTY.exception();
				} else if (StringUtils.isEmpty(genTable.getSubTableFkName())) {
					throw GenErrorCode.SUBTABLE_FK_EMPTY.exception();
				}
			}
		}
	}

	/**
	 * 设置主键列信息
	 * 
	 * @param table
	 *            业务表信息
	 */
	public void setPkColumn(GenTable table) {
		for (GenTableColumn column : table.getColumns()) {
			if (column.isPk()) {
				table.setPkColumn(column);
				break;
			}
		}
		if (Objects.isNull(table.getPkColumn())) {
			table.setPkColumn(table.getColumns().get(0));
		}
		if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
			for (GenTableColumn column : table.getSubTable().getColumns()) {
				if (column.isPk()) {
					table.getSubTable().setPkColumn(column);
					break;
				}
			}
			if (Objects.isNull(table.getSubTable().getPkColumn())) {
				table.getSubTable().setPkColumn(table.getSubTable().getColumns().get(0));
			}
		}
	}

	/**
	 * 设置主子表信息
	 * 
	 * @param table
	 *            业务表信息
	 */
	public void setSubTable(GenTable table) {
		String subTableName = table.getSubTableName();
		if (StringUtils.isNotEmpty(subTableName)) {
			table.setSubTable(genTableMapper.selectGenTableByName(subTableName));
		}
	}

	/**
	 * 设置代码生成其他选项值
	 * 
	 * @param genTable
	 *            设置后的生成对象
	 */
	public void setTableFromOptions(GenTable genTable) {
		if (JacksonUtils.isJson(genTable.getOptions())) {
			String treeCode = JacksonUtils.getAsString(genTable.getOptions(),  GenConstants.TREE_CODE);
			String treeParentCode = JacksonUtils.getAsString(genTable.getOptions(),  GenConstants.TREE_PARENT_CODE);
			String treeName = JacksonUtils.getAsString(genTable.getOptions(),  GenConstants.TREE_NAME);
			String parentMenuId = JacksonUtils.getAsString(genTable.getOptions(),  GenConstants.PARENT_MENU_ID);
			String parentMenuName = JacksonUtils.getAsString(genTable.getOptions(),  GenConstants.PARENT_MENU_NAME);

			genTable.setTreeCode(treeCode);
			genTable.setTreeParentCode(treeParentCode);
			genTable.setTreeName(treeName);
			genTable.setParentMenuId(parentMenuId);
			genTable.setParentMenuName(parentMenuName);
		}
	}

	/**
	 * 获取代码生成地址
	 * 
	 * @param table
	 *            业务表信息
	 * @param template
	 *            模板文件路径
	 * @return 生成地址
	 */
	public static String getGenPath(GenTable table, String template) {
		String genPath = table.getGenPath();
		if (StringUtils.equals(genPath, "/")) {
			return System.getProperty("user.dir") + File.separator + "src" + File.separator
					+ VelocityUtils.getFileName(template, table);
		}
		return genPath + File.separator + VelocityUtils.getFileName(template, table);
	}
}