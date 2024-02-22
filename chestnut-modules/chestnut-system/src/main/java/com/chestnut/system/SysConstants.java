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
package com.chestnut.system;

public interface SysConstants {
	
	/**
	 * 系统新增数据使用的操作人
	 */
	String SYS_OPERATOR = "__system";

	/**
	 * 资源映射路径 前缀
	 */
	String RESOURCE_PREFIX = "/profile/";

	/**
	 * 验证码 redis key
	 */
	String CAPTCHA_CODE_KEY = "captcha_codes:";

	/**
	 * 验证码有效期（分钟）
	 */
	Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 参数管理 cache key
     */
    String CACHE_SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    String CACHE_SYS_DICT_KEY = "sys_dict:";

    /**
     * 部门信息 cache key
     */
    String CACHE_SYS_DEPT_KEY = "sys_dept:";

    /**
     * 角色信息 cache key
     */
    String CACHE_SYS_ROLE_KEY = "sys_role:";

    /**
     * 岗位信息 cache key
     */
    String CACHE_SYS_POST_KEY = "sys_post:";
}
