/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.system.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.enums.MenuType;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
public class CreateMenuRequest extends BaseDTO {

	@NotBlank
	@Length(max = 50)
	private String menuName;

	@NotNull
	@Min(0)
	private Long parentId;

	/** 显示顺序 */
	@NotNull
	private Integer orderNum;

	/** 路由地址 */
	private String path;

	/** 组件路径 */
	private String component;

	/** 路由参数 */
	private String query;

	/** 是否为外链 YesOrNo */
	@Dict(YesOrNo.TYPE)
	private String isFrame;

	/** 是否缓存 YesOrNo */
	@Dict(YesOrNo.TYPE)
	private String isCache;

	/** 类型（M目录 C菜单 F按钮） */
	@NotBlank
	private String menuType;

	/** 显示状态 YesOrNo */
	@Dict(YesOrNo.TYPE)
	private String visible;

	/** 菜单状态 EnableOrDisbale */
	@Dict(EnableOrDisable.TYPE)
	private String status;

	/** 权限字符串 */
	private String perms;

	/** 菜单图标 */
	private String icon;
}
