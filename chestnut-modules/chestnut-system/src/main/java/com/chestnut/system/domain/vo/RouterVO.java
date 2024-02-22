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
package com.chestnut.system.domain.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * 路由配置信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVO {
	
	/**
	 * 路由名字
	 */
	private String name;

	/**
	 * 路由地址
	 */
	private String path;

	/**
	 * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
	 */
	private boolean hidden;

	/**
	 * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
	 */
	private String redirect;

	/**
	 * 组件地址
	 */
	private String component;

	/**
	 * 路由参数：如 {"id": 1, "name": "ry"}
	 */
	private String query;

	/**
	 * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
	 */
	private Boolean alwaysShow;

	/**
	 * 其他元素
	 */
	private MetaVO meta;

	/**
	 * 子路由
	 */
	private List<RouterVO> children;
}
