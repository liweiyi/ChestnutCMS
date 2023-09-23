package com.chestnut.common.extend.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.chestnut.common.extend.enums.XssMode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ConfigurationProperties(prefix = "xss")
public class XssProperties {

	/**
	 * 是否开启XSS过滤
	 */
	private boolean enabled;
	
	/**
	 * 处理方式
	 */
	private XssMode mode;
	
	/**
	 * 不进行处理的路径
	 */
	private List<String> excludes;
	
	/**
	 * 处理指定路径
	 */
	private List<String> urlPatterns;
}
