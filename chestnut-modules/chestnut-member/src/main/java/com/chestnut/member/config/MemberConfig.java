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
package com.chestnut.member.config;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.member.config.properties.MemberProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.FileNotFoundException;

@Slf4j
@Configuration
@EnableConfigurationProperties(MemberProperties.class)
public class MemberConfig implements WebMvcConfigurer {

	/**
	 * 会员资源文件上传目录
	 */
	private static String UPLOAD_DIRECTORY;

	private final MemberProperties properties;

	public MemberConfig(MemberProperties properties) throws FileNotFoundException {
		UPLOAD_DIRECTORY = properties.getUploadPath();
		if (StringUtils.isEmpty(UPLOAD_DIRECTORY)) {
			UPLOAD_DIRECTORY = SpringUtils.getAppParentDirectory() + "/_xy_member/";
		}
		UPLOAD_DIRECTORY = FileExUtils.normalizePath(UPLOAD_DIRECTORY);
		if (!UPLOAD_DIRECTORY.endsWith("/")) {
			UPLOAD_DIRECTORY += "/";
		}
		FileExUtils.mkdirs(UPLOAD_DIRECTORY);
		properties.setUploadPath(UPLOAD_DIRECTORY);
		log.info("Member upload directory: " + UPLOAD_DIRECTORY);
		this.properties = properties;
	}
	
	/**
	 * 获取会员相关资源文件上传根目录
	 */
	public static String getUploadDir() {
		return UPLOAD_DIRECTORY;
	}
	
	/**
	 * 获取资源文件预览地址前缀
	 */
	public static String getResourcePrefix() {
		return "member_preview/";
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		/** 本地文件上传路径 */
		registry.addResourceHandler(getResourcePrefix() + "**")
				.addResourceLocations("file:" + this.properties.getUploadPath());
	}
}
