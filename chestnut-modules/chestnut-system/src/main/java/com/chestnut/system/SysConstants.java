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
