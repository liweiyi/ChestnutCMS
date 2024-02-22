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
package com.chestnut.system.domain;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.i18n.I18nField;
import com.chestnut.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单权限表 sys_menu
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysMenu.TABLE_NAME)
public class SysMenu extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_menu";

	/** 菜单ID */
	@TableId(value = "menu_id", type = IdType.INPUT)
	private Long menuId;

	/** 菜单名称 */
	@I18nField("{MENU.NAME.#{menuId}}")
	private String menuName;

	/** 父菜单名称 */
	@TableField(exist = false)
	private String parentName;

	/** 父菜单ID */
	private Long parentId;

	/** 显示顺序 */
	private Integer orderNum;

	/** 路由地址 */
	private String path;

	/** 组件路径 */
	private String component;

	/** 路由参数 */
	private String query;

	/** 是否为外链 YesOrNo */
	private String isFrame;

	/** 是否缓存 YesOrNo */
	private String isCache;

	/** 类型（M目录 C菜单 F按钮） */
	private String menuType;

	/** 显示状态 YesOrNo */
	private String visible;

	/** 菜单状态 EnableOrDisbale */
	private String status;

	/** 权限字符串 */
	private String perms;

	/** 菜单图标 */
	private String icon;

	/** 子菜单 */
	@TableField(exist = false)
	private List<SysMenu> children = new ArrayList<SysMenu>();

	@NotBlank(message = "菜单名称不能为空")
	@Size(min = 0, max = 50, message = "菜单名称长度不能超过50个字符")
	public String getMenuName() {
		return menuName;
	}

	@NotNull(message = "显示顺序不能为空")
	public Integer getOrderNum() {
		return orderNum;
	}

	@Size(min = 0, max = 200, message = "路由地址不能超过200个字符")
	public String getPath() {
		return path;
	}

	@Size(min = 0, max = 200, message = "组件路径不能超过255个字符")
	public String getComponent() {
		return component;
	}

	@NotBlank(message = "菜单类型不能为空")
	public String getMenuType() {
		return menuType;
	}

	@Size(min = 0, max = 100, message = "权限标识长度不能超过100个字符")
	public String getPerms() {
		return perms;
	}
}
