package com.chestnut.contentcore.config;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.properties.CMSProperties;
import com.chestnut.system.fixed.config.BackendContext;

import freemarker.cache.FileTemplateLoader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * CMS配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CMSProperties.class)
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
}