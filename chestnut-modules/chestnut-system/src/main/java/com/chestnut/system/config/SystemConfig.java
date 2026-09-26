/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.system.config;

import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.config.properties.SysProperties;
import com.chestnut.system.intercepter.DemoModeInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@EnableConfigurationProperties(SysProperties.class)
public class SystemConfig implements WebMvcConfigurer {
	
	/**
	 * 公开访问资源文件上传目录
	 */
	private static String PUBLIC_UPLOAD_DIRECTORY;

	/**
	 * 私有资源文件上传目录
	 */
	private static String PRIVATE_FILE_UPLOAD_DIR;
	
	private final SysProperties properties;

	public SystemConfig(SysProperties properties) {
		String publicDirectory = requireConfiguredPath(properties.getUploadPath(), "chestnut.system.uploadPath");
		String privateDirectory = requireConfiguredPath(properties.getPrivateUploadPath(), "chestnut.system.privateUploadPath");
		Path publicPath = prepareDirectory(publicDirectory);
		Path privatePath = prepareDirectory(privateDirectory);
		if (privatePath.startsWith(publicPath) || publicPath.startsWith(privatePath)) {
			throw new IllegalStateException("Public and private upload directories must not overlap");
		}
		PUBLIC_UPLOAD_DIRECTORY = Strings.CS.appendIfMissing(FileExUtils.normalizePath(publicPath.toString()), "/");
		PRIVATE_FILE_UPLOAD_DIR = Strings.CS.appendIfMissing(FileExUtils.normalizePath(privatePath.toString()), "/");
		properties.setUploadPath(PUBLIC_UPLOAD_DIRECTORY);
		properties.setPrivateUploadPath(PRIVATE_FILE_UPLOAD_DIR);
		log.info("System upload directory: " + PUBLIC_UPLOAD_DIRECTORY);
		log.info("System private upload directory: " + PRIVATE_FILE_UPLOAD_DIR);
		this.properties = properties;
	}

	private static String requireConfiguredPath(String path, String propertyName) {
		if (path == null || path.isBlank()) {
			throw new IllegalStateException(propertyName + " must be configured");
		}
		return path;
	}

	private static Path prepareDirectory(String directory) {
		try {
			return Files.createDirectories(Path.of(directory).toAbsolutePath().normalize()).toRealPath();
		} catch (IOException e) {
			throw new IllegalStateException("Cannot initialize upload directory: " + directory, e);
		}
	}
	
	/**
	 * 获取公共资源文件上传根目录
	 */
	public static String getPublicFileUploadDir() {
		return PUBLIC_UPLOAD_DIRECTORY;
	}

	/**
	 * 获取私有资源文件上传根目录
	 */
	public static String getPrivateFileUploadDir() {
		return PRIVATE_FILE_UPLOAD_DIR;
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
			registry.addInterceptor(new DemoModeInterceptor())
					.addPathPatterns("/**")
					.excludePathPatterns("/login", "/logout", "/captchaImage");
		}
	}
}
