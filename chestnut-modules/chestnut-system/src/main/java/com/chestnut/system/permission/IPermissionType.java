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
package com.chestnut.system.permission;

import cn.dev33.satoken.annotation.SaMode;

import java.util.List;
import java.util.Set;

/**
 * 权限类型
 * 
 * 系统模块提供权限持久化，各类型权限存储格式各自定义
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPermissionType {

	String BEAN_PREFIX = "PermissionType_";
	
	/**
	 * 权限字符串分隔符
	 */
	String Spliter = ":";
	
	/**
	 * 类型唯一标识
	 */
	String getId();

	/**
	 * 类型名称
	 */
	String getName();
	
	/**
	 * 将存储在sys_permission中的权限字符串解析成权限项集合
	 *
	 * 实现此方法的权限项集合会加入SA-TOKEN的权限列表中。
	 * @see com.chestnut.system.security.AdminUserType#getPermissionList
	 *
	 * @param json
	 * @return
	 */
	Set<String> deserialize(String json);

	/**
	 * 转权限项列表转成持久化存储字符串
	 * 
	 * @param permissionKeys
	 * @return
	 */
	String serialize(Set<String> permissionKeys);

	/**
	 * 是否有权限
	 * 
	 * @param permissionKeys
	 * @param json
	 * @param mode
	 * @return
	 */
	boolean hasPermission(List<String> permissionKeys, String json, SaMode mode);
}
