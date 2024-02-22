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
package com.chestnut.common.log.enums;

/**
 * 业务操作类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum BusinessType {
	/**
	 * 其它
	 */
	OTHER,

	/**
	 * 新增
	 */
	INSERT,

	/**
	 * 修改
	 */
	UPDATE,

	/**
	 * 删除
	 */
	DELETE,

	/**
	 * 授权
	 */
	GRANT,

	/**
	 * 导出
	 */
	EXPORT,

	/**
	 * 导入
	 */
	IMPORT,

	/**
	 * 强退
	 */
	FORCE,

	/**
	 * 生成代码
	 */
	GENCODE,

	/**
	 * 清空数据
	 */
	CLEAN,
}
