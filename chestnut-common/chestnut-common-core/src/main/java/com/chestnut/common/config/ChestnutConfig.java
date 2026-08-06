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
package com.chestnut.common.config;

import com.chestnut.common.config.properties.ChestnutImageProperties;
import com.chestnut.common.config.properties.ChestnutProperties;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.ImageProcessor;
import com.chestnut.common.utils.image.ImageUtils;
import com.chestnut.common.utils.image.JDKImageProcessor;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ser.std.ToStringSerializer;
import tools.jackson.datatype.guava.GuavaModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/**
 * 读取项目相关配置
 *
 * @author 兮玥（190785909@qq.com）
 */
@Slf4j
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableConfigurationProperties({ ChestnutProperties.class, ChestnutImageProperties.class })
public class ChestnutConfig {

	public ChestnutConfig() {
		loadFonts();
		initImageProcessor();
	}
	
	@Bean
	public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
		return builder -> {
	        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
			builder.defaultDateFormat(new SimpleDateFormat(dateTimeFormat));
			builder.defaultTimeZone(TimeZone.getDefault());
			//配置序列化级别
			builder.changeDefaultPropertyInclusion(value -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
	        //配置JSON缩进支持
			builder.disable(SerializationFeature.INDENT_OUTPUT);
	        //允许单个数值当做数组处理
			builder.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	        //禁止重复键, 抛出异常
			builder.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			//有属性不能映射的时候不报错
			builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        //对象为空时不抛异常
			builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

			List<JacksonModule> modules = new ArrayList<>();
			SimpleModule simpleModule = new SimpleModule();
			// 长整型数字转字符串
			simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
			simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
			simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
			simpleModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
	        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)))
	                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateTimeFormat)));
			modules.add(simpleModule);
	        //识别Guava包的类
	        modules.add(new GuavaModule());
	        builder.addModules(modules);
		};
	}

	/**
	 * 加载用户字体
	 */
	public void loadFonts() {
		try {
			// 获取图形环境
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
			Resource[] resources = resourcePatternResolver.getResources("classpath*:/fonts/*.ttf");
			for (Resource resource : resources) {
				try (InputStream is = resource.getInputStream()) {
					Font font = Font.createFont(Font.TRUETYPE_FONT, is);
					// 派生出一个默认大小的字体（注册时不需要，但可用于测试）
					Font derivedFont = font.deriveFont(12f);
					// 注册字体
					if (ge.registerFont(derivedFont)) {
						log.info("Font registered: " + font.getFontName() + ", family: " + font.getFamily());
					}
				} catch (FontFormatException e) {
					log.warn("Font register fail: " + resource.getURI());
				}
			}
		} catch (IOException e) {
			log.warn("Font register failed.", e);
		}
	}

	private void initImageProcessor() {
		ChestnutImageProperties properties = SpringUtils.getBean(ChestnutImageProperties.class);
		Map<String, ImageProcessor> imageProcessorMap = SpringUtils.getBeanMap(ImageProcessor.class);
		ImageProcessor imageProcessor = null;
		if (StringUtils.isNotEmpty(properties.getType())) {
			imageProcessor = imageProcessorMap.get(ImageProcessor.BEAN_PREFIX + properties.getType());
		}
		if (Objects.isNull(imageProcessor)) {
			imageProcessor = imageProcessorMap.get(ImageProcessor.BEAN_PREFIX + JDKImageProcessor.ID);
		}
		ImageUtils.setImageProcessor(imageProcessor);
	}
}
