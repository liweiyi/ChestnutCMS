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
package com.chestnut.system.exception;

import com.chestnut.common.exception.ErrorCode;

public enum SysErrorCode implements ErrorCode {
	
	/**
	 * 用户/密码必须填写
	 */
	UNAME_PWD_REQUIRED,
	
	/**
	 * 用户不存在
	 */
	USER_NOT_EXISTS,
	
	/**
	 * 用户被锁定
	 */
	USER_LOCKED,
	
	/**
	 * 用户被禁用
	 */
	USER_DISABLED,
	
	/**
	 * Lang: 密码错误
	 */
	PASSWORD_ERROR, 
	
	/**
	 * Lang: 安全校验失败：{0}
	 */
	SECURITY_AUTH_FAIL,

	/**
	 * Lang: 验证码已过期
	 */
	CAPTCHA_EXPIRED,

	/**
	 * Lang: 验证码错误
	 */
	CAPTCHA_ERR,
	
	/**
	 * Lang: 密码不符合安全校验规则
	 */
	INSECURE_PASSWORD,
	
	/**
	 * 系统默认机构不可删除
	 */
	ORG_DEL_ROOT,
	
	/**
	 * 机构删失败：请先删除子机构
	 */
	ORG_DEL_CHILD,
	
	/**
	 * 机构删失败：请先移除机构关联角色
	 */
	ORG_DEL_ROLE,
	
	/**
	 * 机构删失败：请先移除机构关联用户
	 */
	ORG_DEL_USER,
	
	/**
	 * 用户关联角色与机构不匹配
	 */
	USER_ROLE_ORG_MATCH,
	
	/**
	 * 不允许删除超级管理员
	 */
	SUPERADMIN_DELETE, 
	
	/**
	 * Lang：未配置文件存储方式
	 */
	STORAGE_CONFIG_UNEXISTS,
	
	/**
	 * "部门停用，不允许新增"
	 */
	DISBALE_DEPT_ADD_CHILD,
	
	/**
	 * 请先删除字典数据项
	 */
	DELETE_DICT_DATA_FIRST, 
	
	/**
	 * 已存在的字典类型：{0}
	 */
	DICT_TYPE_CONFLICT,
	
	/**
	 * 请先删除岗位“{0}”关联的用户
	 */
	POST_USER_NOT_EMPTY,
	
	/**
	 * 请先删除角色“{0}”关联的用户
	 */
	ROLE_USER_NOT_EMPTY,
	
	/**
	 * 暂未开放账号注册
	 */
	REGIST_DISABELD,
	
	/**
	 * 上传文件大小不能超过：{0}
	 */
	UPLOAD_FILE_SIZE_LIMIT,
	
	/**
	 * 不支持上传此类型文件
	 */
	UPLOAD_FILE_TYPE_LIMIT,
	
	/**
	 * 后台验证码配置错误
	 */
	CAPTCHA_CONFIG_ERR,
	
	/**
	 * 请先删除子菜单
	 */
	MENU_DEL_CHILD_FIRST,
	
	/**
	 * 不支持的用户偏好配置：{0}
	 */
	UNSUPPORTED_USER_PREFERENCE,
	
	/**
	 * 用户偏好配置“{0}”数据校验失败
	 */
	INVALID_USER_PREFERENCE,
	
	/**
	 * 指定任务不存在
	 */
	ASYNC_TASK_NOT_FOUND,
	
	/**
	 * 不支持的权限类型：{0}
	 */
	UNSUPPORTED_PERMISSION_TYPE, 
	
	/**
	 * 启用状态的定时任务不可修改
	 */
	SCHEDULED_TASK_UPDATE_ERR,
	
	/**
	 * 不能删除启用状态的任务
	 */
	SCHEDULED_TASK_REMOVE_ERR,
	
	/**
	 * 定时任务“{0}”已存在
	 */
	SCHEDULED_TASK_EXISTS,
	
	/**
	 * 只能手动执行停用状态任务
	 */
	SCHEDULED_TASK_EXEC_ERR,

	/**
	 * 不支持的定时任务类型：{0}
	 */
	SCHEDULED_TASK_UNSUPPORTED_HANDLER,

	/**
	 * 任务触发器`{0}`配置错误：{1}
	 */
	SCHEDULED_TASK_TRIGGER_ERR,

	/**
	 * 任务正在运行中
	 */
	SCHEDULED_TASK_RUNNING,

	/**
	 * 不支持的定时任务触发器类型：{0}
	 */
	SCHEDULED_TASK_UNSUPPORTED_TRIGGER;
	
	@Override
	public String value() {
		return "{ERRCODE.SYS." + this.name() + "}";
	}
}
