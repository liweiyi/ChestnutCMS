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
package com.chestnut.common.exception;

public enum CommonErrorCode implements ErrorCode {
	
	/**
	 * Lang: 参数【{0}】不能为空
	 */
	NOT_EMPTY,

	/**
	 * Lang: 数据不存在
	 */
	DATA_NOT_FOUND,

	/**
	 * Lang: 指定ID[{0}:{1}]数据不存在
	 */
	DATA_NOT_FOUND_BY_ID,

	/**
	 * Lang:数据[{0}]冲突
	 */
	DATA_CONFLICT,
	
	/**
	 * Lang: 请求参数【{0}】不符合校验规则
	 */
	INVALID_REQUEST_ARG,
	
	/**
	 * Lang: 系统错误
	 */
	SYSTEM_ERROR,
	
	/**
	 * Lang: 未知错误
	 */
	UNKNOWN_ERROR,
	
	/**
	 * Lang: 数据库操作失败
	 */
	DATABASE_FAIL, 
	
	/**
	 * Lang: Http(s)请求失败：{0}
	 */
	REQUEST_FAILED,
	
	/**
	 * 系统固定字典数据不允许删除或修改类型
	 */
	FIXED_DICT,

	/**
	 * 此系统固定字典类型不予许添加子项
	 */
	FIXED_DICT_NOT_ALLOW_ADD,

	/**
	 * 系统固定配置参数[{0}]不能删除
	 */
	FIXED_CONFIG_DEL,

	/**
	 * 系统固定配置参数[{0}]不能修改键名
	 */
	FIXED_CONFIG_UPDATE,
	
	/**
	 * 任务“{0}”正在运行中
	 */
	ASYNC_TASK_RUNNING;
	
	@Override
	public String value() {
		return "{ERRCODE.COMMON." + this.name() + "}";
	}
}
