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
package com.chestnut.generator.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.velocity.VelocityContext;

import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.generator.GenConstants;
import com.chestnut.generator.domain.GenTable;
import com.chestnut.generator.domain.GenTableColumn;

/**
 * 模板处理工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class VelocityUtils {
	/** 项目空间路径 */
	private static final String PROJECT_PATH = "main/java";

	/** mybatis空间路径 */
	private static final String MYBATIS_PATH = "main/resources/mapper";

	/** 默认上级菜单，系统工具 */
	private static final String DEFAULT_PARENT_MENU_ID = "3";

	/**
	 * 设置模板变量信息
	 *
	 * @return 模板列表
	 */
	public static VelocityContext prepareContext(GenTable genTable) {
		String moduleName = genTable.getModuleName();
		String businessName = genTable.getBusinessName();
		String packageName = genTable.getPackageName();
		String tplCategory = genTable.getTplCategory();
		String functionName = genTable.getFunctionName();

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("tplCategory", genTable.getTplCategory());
		velocityContext.put("tableName", genTable.getTableName());
		velocityContext.put("functionName", StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
		velocityContext.put("ClassName", genTable.getClassName());
		velocityContext.put("className", StringUtils.uncapitalize(genTable.getClassName()));
		velocityContext.put("moduleName", genTable.getModuleName());
		velocityContext.put("BusinessName", StringUtils.capitalize(genTable.getBusinessName()));
		velocityContext.put("businessName", genTable.getBusinessName());
		velocityContext.put("basePackage", getPackagePrefix(packageName));
		velocityContext.put("packageName", packageName);
		velocityContext.put("author", genTable.getFunctionAuthor());
		velocityContext.put("datetime", DateUtils.getDate());
		velocityContext.put("pkColumn", genTable.getPkColumn());
		velocityContext.put("importList", getImportList(genTable));
		velocityContext.put("permissionPrefix", getPermissionPrefix(moduleName, businessName));
		velocityContext.put("columns", genTable.getColumns());
		velocityContext.put("table", genTable);
		velocityContext.put("dicts", getDicts(genTable));
		setMenuVelocityContext(velocityContext, genTable);
		if (GenConstants.TPL_TREE.equals(tplCategory)) {
			setTreeVelocityContext(velocityContext, genTable);
		}
		if (GenConstants.TPL_SUB.equals(tplCategory)) {
			setSubVelocityContext(velocityContext, genTable);
		}
		return velocityContext;
	}

	public static void setMenuVelocityContext(VelocityContext context, GenTable genTable) {
		String parentMenuId = getParentMenuId(genTable.getOptions());
		context.put("parentMenuId", parentMenuId);
	}

	public static void setTreeVelocityContext(VelocityContext context, GenTable genTable) {
		String options = genTable.getOptions();
		String treeCode = getTreecode(options);
		String treeParentCode = getTreeParentCode(options);
		String treeName = getTreeName(options);

		context.put("treeCode", treeCode);
		context.put("treeParentCode", treeParentCode);
		context.put("treeName", treeName);
		context.put("expandColumn", getExpandColumn(genTable));
		Map<String, Object> optionsMap = JacksonUtils.fromMap(options);
		if (optionsMap.containsKey(GenConstants.TREE_PARENT_CODE)) {
			context.put("tree_parent_code", optionsMap.get(GenConstants.TREE_PARENT_CODE));
		}
		if (optionsMap.containsKey(GenConstants.TREE_NAME)) {
			context.put("tree_name", optionsMap.get(GenConstants.TREE_NAME));
		}
	}

	public static void setSubVelocityContext(VelocityContext context, GenTable genTable) {
		GenTable subTable = genTable.getSubTable();
		String subTableName = genTable.getSubTableName();
		String subTableFkName = genTable.getSubTableFkName();
		String subClassName = genTable.getSubTable().getClassName();
		String subTableFkClassName = StringUtils.convertToCamelCase(subTableFkName);

		context.put("subTable", subTable);
		context.put("subTableName", subTableName);
		context.put("subTableFkName", subTableFkName);
		context.put("subTableFkClassName", subTableFkClassName);
		context.put("subTableFkclassName", StringUtils.uncapitalize(subTableFkClassName));
		context.put("subClassName", subClassName);
		context.put("subclassName", StringUtils.uncapitalize(subClassName));
		context.put("subImportList", getImportList(genTable.getSubTable()));
	}

	/**
	 * 获取模板信息
	 *
	 * @return 模板列表
	 */
	public static List<String> getTemplateList(String tplCategory) {
		List<String> templates = new ArrayList<String>();
		templates.add("vm/java/domain.java.vm");
		templates.add("vm/java/mapper.java.vm");
		templates.add("vm/java/service.java.vm");
		templates.add("vm/java/serviceImpl.java.vm");
		templates.add("vm/java/controller.java.vm");
		templates.add("vm/xml/mapper.xml.vm");
		templates.add("vm/sql/sql.vm");
		templates.add("vm/js/api.js.vm");
		if (GenConstants.TPL_CRUD.equals(tplCategory)) {
			templates.add("vm/vue/index.vue.vm");
		} else if (GenConstants.TPL_TREE.equals(tplCategory)) {
			templates.add("vm/vue/index-tree.vue.vm");
		} else if (GenConstants.TPL_SUB.equals(tplCategory)) {
			templates.add("vm/vue/index.vue.vm");
			templates.add("vm/java/sub-domain.java.vm");
		}
		return templates;
	}

	/**
	 * 获取文件名
	 */
	public static String getFileName(String template, GenTable genTable) {
		// 文件名称
		String fileName = "";
		// 包路径
		String packageName = genTable.getPackageName();
		// 模块名
		String moduleName = genTable.getModuleName();
		// 大写类名
		String className = genTable.getClassName();
		// 业务名称
		String businessName = genTable.getBusinessName();

		String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/");
		String mybatisPath = MYBATIS_PATH + "/" + moduleName;
		String vuePath = "vue";

		if (template.contains("domain.java.vm")) {
			fileName = StringUtils.messageFormat("{0}/domain/{1}.java", javaPath, className);
		}
		if (template.contains("sub-domain.java.vm")
				&& StringUtils.equals(GenConstants.TPL_SUB, genTable.getTplCategory())) {
			fileName = StringUtils.messageFormat("{0}/domain/{1}.java", javaPath, genTable.getSubTable().getClassName());
		} else if (template.contains("mapper.java.vm")) {
			fileName = StringUtils.messageFormat("{0}/mapper/{1}Mapper.java", javaPath, className);
		} else if (template.contains("service.java.vm")) {
			fileName = StringUtils.messageFormat("{0}/service/I{1}Service.java", javaPath, className);
		} else if (template.contains("serviceImpl.java.vm")) {
			fileName = StringUtils.messageFormat("{0}/service/impl/{1}ServiceImpl.java", javaPath, className);
		} else if (template.contains("controller.java.vm")) {
			fileName = StringUtils.messageFormat("{0}/controller/{1}Controller.java", javaPath, className);
		} else if (template.contains("mapper.xml.vm")) {
			fileName = StringUtils.messageFormat("{0}/{1}Mapper.xml", mybatisPath, className);
		} else if (template.contains("sql.vm")) {
			fileName = businessName + "Menu.sql";
		} else if (template.contains("api.js.vm")) {
			fileName = StringUtils.messageFormat("{0}/api/{1}/{2}.js", vuePath, moduleName, businessName);
		} else if (template.contains("index.vue.vm")) {
			fileName = StringUtils.messageFormat("{0}/views/{1}/{2}/index.vue", vuePath, moduleName, businessName);
		} else if (template.contains("index-tree.vue.vm")) {
			fileName = StringUtils.messageFormat("{0}/views/{1}/{2}/index.vue", vuePath, moduleName, businessName);
		}
		return fileName;
	}

	/**
	 * 获取包前缀
	 *
	 * @param packageName 包名称
	 * @return 包前缀名称
	 */
	public static String getPackagePrefix(String packageName) {
		int lastIndex = packageName.lastIndexOf(".");
		return StringUtils.substring(packageName, 0, lastIndex);
	}

	/**
	 * 根据列类型获取导入包
	 * 
	 * @param genTable 业务表对象
	 * @return 返回需要导入的包列表
	 */
	public static HashSet<String> getImportList(GenTable genTable) {
		List<GenTableColumn> columns = genTable.getColumns();
		GenTable subGenTable = genTable.getSubTable();
		HashSet<String> importList = new HashSet<String>();
		if (Objects.nonNull(subGenTable)) {
			importList.add("java.util.List");
		}
		for (GenTableColumn column : columns) {
			if (!column.isSuperColumn() && GenConstants.TYPE_DATE.equals(column.getJavaType())) {
				importList.add("java.util.Date");
				importList.add("com.fasterxml.jackson.annotation.JsonFormat");
			} else if (!column.isSuperColumn() && GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
				importList.add("java.math.BigDecimal");
			}
		}
		return importList;
	}

	/**
	 * 根据列类型获取字典组
	 * 
	 * @param genTable 业务表对象
	 * @return 返回字典组
	 */
	public static String getDicts(GenTable genTable) {
		List<GenTableColumn> columns = genTable.getColumns();
		Set<String> dicts = new HashSet<String>();
		addDicts(dicts, columns);
		if (Objects.nonNull(genTable.getSubTable())) {
			List<GenTableColumn> subColumns = genTable.getSubTable().getColumns();
			addDicts(dicts, subColumns);
		}
		return StringUtils.join(dicts, ", ");
	}

	/**
	 * 添加字典列表
	 * 
	 * @param dicts   字典列表
	 * @param columns 列集合
	 */
	public static void addDicts(Set<String> dicts, List<GenTableColumn> columns) {
		for (GenTableColumn column : columns) {
			if (!column.isSuperColumn() && StringUtils.isNotEmpty(column.getDictType()) && StringUtils.equalsAny(
					column.getHtmlType(),
					new String[] { GenConstants.HTML_SELECT, GenConstants.HTML_RADIO, GenConstants.HTML_CHECKBOX })) {
				dicts.add("'" + column.getDictType() + "'");
			}
		}
	}

	/**
	 * 获取权限前缀
	 *
	 * @param moduleName   模块名称
	 * @param businessName 业务名称
	 * @return 返回权限前缀
	 */
	public static String getPermissionPrefix(String moduleName, String businessName) {
		return StringUtils.messageFormat("{0}:{1}", moduleName, businessName);
	}

	/**
	 * 获取上级菜单ID字段
	 *
	 * @param paramsObj 生成其他选项
	 * @return 上级菜单ID字段
	 */
	public static String getParentMenuId(String paramsJson) {
		if (JacksonUtils.isJson(paramsJson)) {
			String value = JacksonUtils.getAsString(paramsJson, GenConstants.PARENT_MENU_ID);
			if (StringUtils.isNotEmpty(value)) {
				return value;
			}
		}
		return DEFAULT_PARENT_MENU_ID;
	}

	/**
	 * 获取树编码
	 *
	 * @param paramsObj 生成其他选项
	 * @return 树编码
	 */
	public static String getTreecode(String paramsJson) {
		if (JacksonUtils.isJson(paramsJson)) {
			String value = JacksonUtils.getAsString(paramsJson, GenConstants.TREE_CODE);
			if (StringUtils.isNotEmpty(value)) {
				return StringUtils.toCamelCase(value);
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 获取树父编码
	 *
	 * @param paramsObj 生成其他选项
	 * @return 树父编码
	 */
	public static String getTreeParentCode(String paramsJson) {
		if (JacksonUtils.isJson(paramsJson)) {
			String value = JacksonUtils.getAsString(paramsJson, GenConstants.TREE_PARENT_CODE);
			if (StringUtils.isNotEmpty(value)) {
				return StringUtils.toCamelCase(value);
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 获取树名称
	 *
	 * @param paramsObj 生成其他选项
	 * @return 树名称
	 */
	public static String getTreeName(String paramsJson) {
		if (JacksonUtils.isJson(paramsJson)) {
			String value = JacksonUtils.getAsString(paramsJson, GenConstants.TREE_NAME);
			if (StringUtils.isNotEmpty(value)) {
				return StringUtils.toCamelCase(value);
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 获取需要在哪一列上面显示展开按钮
	 *
	 * @param genTable 业务表对象
	 * @return 展开按钮列序号
	 */
	public static int getExpandColumn(GenTable genTable) {
		String options = genTable.getOptions();
		String treeName = JacksonUtils.getAsString(options, GenConstants.TREE_NAME);
		int num = 0;
		for (GenTableColumn column : genTable.getColumns()) {
			if (column.isList()) {
				num++;
				String columnName = column.getColumnName();
				if (columnName.equals(treeName)) {
					break;
				}
			}
		}
		return num;
	}
}
