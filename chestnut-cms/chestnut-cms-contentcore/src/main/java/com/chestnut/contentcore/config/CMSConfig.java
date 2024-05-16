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
package com.chestnut.contentcore.config;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.properties.CMSProperties;
import com.chestnut.contentcore.config.properties.CMSPublishProperties;
import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.strategies.ThreadPoolPublishStrategy;
import com.chestnut.system.fixed.config.BackendContext;
import freemarker.cache.FileTemplateLoader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * CMS配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ CMSProperties.class, CMSPublishProperties.class })
public class CMSConfig implements WebMvcConfigurer {

	public static String CachePrefix = "cms:";

	private static String CACHE_PREFIX;

	private static String RESOURCE_ROOT;
	
	private final RedisCache redisCache;
	
	private final CMSProperties properties;
	
	public CMSConfig(CMSProperties properties, RedisCache redisCache) {
		// CMS缓存前缀
		CACHE_PREFIX = properties.getCacheName();
		// 站点资源存放根目录
		RESOURCE_ROOT = properties.getResourceRoot();
		if (StringUtils.isEmpty(RESOURCE_ROOT)) {
			RESOURCE_ROOT = SpringUtils.getAppParentDirectory() + "/wwwroot_release/";
		}
		RESOURCE_ROOT = FileExUtils.normalizePath(RESOURCE_ROOT);
		if (!RESOURCE_ROOT.endsWith("/")) {
			RESOURCE_ROOT += "/";
		}
		FileExUtils.mkdirs(RESOURCE_ROOT);
		properties.setResourceRoot(RESOURCE_ROOT);
		log.info("ResourceRoot: " + RESOURCE_ROOT);
		CachePrefix = properties.getCacheName();
		
		this.properties = properties;
		this.redisCache = redisCache;
	}
	
	@Bean
	public FileTemplateLoader cmsFileTemplateLoader() throws IOException {
		return new FileTemplateLoader(new File(RESOURCE_ROOT));
	}

	public String getCachePrefix() {
		return CACHE_PREFIX;
	}

	/**
	 * 获取本地资源存储根目录
	 * <p>
	 * 如果未配置，开发环境取项目根目录下wwwroot_release，部署环境取项目部署同级目录wwwroot_release
	 * </p>
	 */
	public static String getResourceRoot() {
		return RESOURCE_ROOT;
	}

	public static String getResourcePreviewPrefix() {
		return BackendContext.getValue() + ContentCoreConsts.RESOURCE_PREVIEW_PREFIX;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		/** 本地文件上传路径 */
		registry.addResourceHandler(ContentCoreConsts.RESOURCE_PREVIEW_PREFIX + "**")
				.addResourceLocations("file:" + getResourceRoot());
	}
	
	@PostConstruct
	public void resetCache() {
		if (this.properties.getResetCache()) {
			Collection<String> keys = this.redisCache.keys(this.properties.getCacheName() + "*");
			this.redisCache.deleteObject(keys);
			log.info("Clear redis caches with prefix `{}`", this.properties.getCacheName());
		}
	}

	@Bean
	@ConditionalOnMissingBean(IPublishStrategy.class)
	public IPublishStrategy publishStrategy(CMSPublishProperties publishProperties, CmsStaticizeService cmsStaticizeService) {
		return new ThreadPoolPublishStrategy(publishProperties, cmsStaticizeService);
	}
}