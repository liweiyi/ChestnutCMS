package com.chestnut.system.config;

import java.io.FileNotFoundException;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.config.properties.SysProperties;
import com.chestnut.system.intercepter.DemoModeIntercepter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(SysProperties.class)
public class SystemConfig implements WebMvcConfigurer {
	
	/**
	 * 通用资源文件上传目录
	 */
	private static String UPLOAD_DIRECTORY;
	
	private final SysProperties properties;
	
	public SystemConfig(SysProperties properties) throws FileNotFoundException {
		UPLOAD_DIRECTORY = properties.getUploadPath();
		if (StringUtils.isEmpty(UPLOAD_DIRECTORY)) {
			UPLOAD_DIRECTORY = SpringUtils.getAppParentDirectory() + "/profile/";
		}
		UPLOAD_DIRECTORY = FileExUtils.normalizePath(UPLOAD_DIRECTORY);
		if (!UPLOAD_DIRECTORY.endsWith("/")) {
			UPLOAD_DIRECTORY += "/";
		}
		FileExUtils.mkdirs(UPLOAD_DIRECTORY);
		properties.setUploadPath(UPLOAD_DIRECTORY);
		log.info("System upload directory: " + UPLOAD_DIRECTORY);
		this.properties = properties;
	}
	
	/**
	 * 获取资源文件上传根目录
	 */
	public static String getUploadDir() {
		return UPLOAD_DIRECTORY;
	}
	
	/**
	 * 获取资源文件预览地址前缀
	 */
	public static String getResourcePrefix() {
		return SysConstants.RESOURCE_PREFIX;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		/** 本地文件上传路径 */
		registry.addResourceHandler(getResourcePrefix() + "**")
				.addResourceLocations("file:" + this.properties.getUploadPath());
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 演示模式
		if (properties.isDemoMode()) {
			registry.addInterceptor(new DemoModeIntercepter()).addPathPatterns("/**").excludePathPatterns("/login",
					"/logout", "/captchaImage");
		}
	}
}
