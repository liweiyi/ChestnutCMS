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
package com.chestnut.common.staticize.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chestnut.common.staticize.config.properties.FreeMarkerProperties;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MruCacheStorage;
import freemarker.cache.MultiTemplateLoader;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Configuration
@EnableConfigurationProperties(FreeMarkerProperties.class)
public class FreeMarkerConfig {

	/**
	 * FreeMarker配置类全局唯一，其配置参数可被Template或Environment配置覆盖。
	 * 
	 * <p>
	 * 配置生效顺序：Configuration -> Template -> Environment
	 * </p>
	 * 
	 * @param properties
	 * @param fileTemplateLoader
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	@Bean("staticizeConfiguration")
	@ConditionalOnMissingBean(freemarker.template.Configuration.class)
	freemarker.template.Configuration staticizeFreeMarkerConfiguration(final FreeMarkerProperties properties,
			final List<FileTemplateLoader> fileTemplateLoaders) throws IOException, TemplateException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(
				freemarker.template.Configuration.VERSION_2_3_31);
		cfg.setDefaultEncoding(properties.getDefaultEncoding());
		// 模板加载路径
		MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(
				fileTemplateLoaders.toArray(FileTemplateLoader[]::new));
		cfg.setTemplateLoader(multiTemplateLoader);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		// 默认模板缓存策略：Most recently use cache.
		// 缓存分两级，强引用->弱引用，强引用数达到上限则会将使用次数更少的转移到弱引用缓存，强引用不会被JVM释放，弱引用则相反。
		// 默认设置：strongSizeLimit = 100，softSizeLimit = 1000
		cfg.setCacheStorage(new MruCacheStorage(properties.getMruCache().getStrongSizeLimit(),
				properties.getMruCache().getSoftSizeLimit()));
		cfg.setNumberFormat("0.##");
		// settings
		if (Objects.nonNull(properties.getSettings()) && !properties.getSettings().isEmpty()) {
			Properties settings = new Properties();
			settings.putAll(properties.getSettings());
			cfg.setSettings(settings);
		}
		return cfg;
	}

	@Bean
	FileTemplateLoader defaultFileTemplateLoader(FreeMarkerProperties properties) throws IOException {
		String templateLoaderPath = properties.getTemplateLoaderPath();
		if (StringUtils.isEmpty(templateLoaderPath)) {
			// 默认取当前项目部署路径同级目录statics作为模板目录
			templateLoaderPath = SpringUtils.getAppParentDirectory() + "/statics/";
		}
		templateLoaderPath = FileExUtils.normalizePath(templateLoaderPath);
		if (!templateLoaderPath.endsWith("/")) {
			templateLoaderPath += "/";
		}
		FileExUtils.mkdirs(templateLoaderPath);
		properties.setTemplateLoaderPath(templateLoaderPath);
		return new FileTemplateLoader(new File(templateLoaderPath));
	}
}
