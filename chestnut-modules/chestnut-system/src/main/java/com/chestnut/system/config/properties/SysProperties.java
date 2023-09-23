package com.chestnut.system.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.system")
public class SysProperties {

	/**
	 * 资源文件上传根目录
	 */
	private String uploadPath;
	
	/**
	 * 后台登录注册验证码类型配置
	 */
	private String captchaType;
	
	/** 演示模式开关 */
	private boolean demoMode;
}
