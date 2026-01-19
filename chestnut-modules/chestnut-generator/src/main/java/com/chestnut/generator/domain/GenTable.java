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
package com.chestnut.generator.domain;

import java.io.Serial;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.generator.GenConstants;

/**
 * 业务表 gen_table
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Setter
@Getter
@TableName(GenTable.TABLE_NAME)
public class GenTable extends BaseEntity {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "gen_table";

	/** 编号 */
    @TableId(value = "table_id", type = IdType.INPUT)
	private Long tableId;

	/** 表名称 */
	@NotBlank(message = "表名称不能为空")
	private String tableName;

	/** 表描述 */
	@NotBlank(message = "表描述不能为空")
	private String tableComment;

	/** 关联父表的表名 */
	private String subTableName;

	/** 本表关联父表的外键名 */
	private String subTableFkName;

	/** 实体类名称(首字母大写) */
	@NotBlank(message = "实体类名称不能为空")
	private String className;

	/** 使用的模板（crud单表操作 tree树表操作 sub主子表操作） */
	private String tplCategory;

	/** 生成包路径 */
	@NotBlank(message = "生成包路径不能为空")
	private String packageName;

	/** 生成模块名 */
	@NotBlank(message = "生成模块名不能为空")
	private String moduleName;

	/** 生成业务名 */
	@NotBlank(message = "生成业务名不能为空")
	private String businessName;

	/** 生成功能名 */
	@NotBlank(message = "生成功能名不能为空")
	private String functionName;

	/** 生成作者 */
	@NotBlank(message = "作者不能为空")
	private String functionAuthor;

	/** 生成代码方式（0zip压缩包 1自定义路径） */
	private String genType;

	/** 生成路径（不填默认项目路径） */
	private String genPath;

	/** 主键信息 */
	@TableField(exist = false)
	private GenTableColumn pkColumn;

	/** 子表信息 */
	@TableField(exist = false)
	private GenTable subTable;

	/** 表列信息 */
	@Valid
	@TableField(exist = false)
	private List<GenTableColumn> columns;

	/** 其它生成选项 */
	private String options;

	/** 树编码字段 */
	@TableField(exist = false)
	private String treeCode;

	/** 树父编码字段 */
	@TableField(exist = false)
	private String treeParentCode;

	/** 树名称字段 */
	@TableField(exist = false)
	private String treeName;

	/** 上级菜单ID字段 */
	@TableField(exist = false)
	private String parentMenuId;

	/** 上级菜单名称字段 */
	@TableField(exist = false)
	private String parentMenuName;

	public boolean isSub() {
		return isSub(this.tplCategory);
	}

	public static boolean isSub(String tplCategory) {
		return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
	}

	public boolean isTree() {
		return isTree(this.tplCategory);
	}

	public static boolean isTree(String tplCategory) {
		return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
	}

	public boolean isCrud() {
		return isCrud(this.tplCategory);
	}

	public static boolean isCrud(String tplCategory) {
		return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
	}

	public boolean isSuperColumn(String javaField) {
		return isSuperColumn(this.tplCategory, javaField);
	}

	public static boolean isSuperColumn(String tplCategory, String javaField) {
		if (isTree(tplCategory)) {
			return StringUtils.equalsAnyIgnoreCase(javaField,
					ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
		}
		return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
	}
}