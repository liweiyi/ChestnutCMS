package com.chestnut.system.fixed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 固定参数项
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@AllArgsConstructor
public class FixedConfig {
	
	public static final String BEAN_PREFIX = "FIXED_CONFIG.";

	/**
	 * 固定配置参数键名
	 */
	private String key;

	/**
	 * 固定配置参数名称
	 */
	private String name;

	/**
	 * 默认值
	 */
	private String defaultValue;

	/**
	 * 备注
	 */
	private String remark;
}
