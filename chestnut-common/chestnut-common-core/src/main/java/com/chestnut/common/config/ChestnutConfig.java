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
package com.chestnut.common.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.chestnut.common.config.properties.ChestnutProperties;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * 读取项目相关配置
 *
 * @author 兮玥（190785909@qq.com）
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableConfigurationProperties(ChestnutProperties.class)
public class ChestnutConfig {
	
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return builder -> {
	        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
			builder.dateFormat(new SimpleDateFormat(dateTimeFormat));
			builder.timeZone(TimeZone.getDefault());
			//配置序列化级别
			builder.serializationInclusion(JsonInclude.Include.NON_NULL);
	        //配置JSON缩进支持
			builder.featuresToDisable(SerializationFeature.INDENT_OUTPUT);
	        //允许单个数值当做数组处理
			builder.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	        //禁止重复键, 抛出异常
			builder.featuresToEnable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
	        // 禁止使用int代表Enum的order()來反序列化Enum
			builder.featuresToEnable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
	        //有属性不能映射的时候不报错
			builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        //对象为空时不抛异常
			builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	        //时间格式
			builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	        //允许未知字段
			builder.featuresToEnable(JsonGenerator.Feature.IGNORE_UNKNOWN);
	        //序列化BigDecimal时之间输出原始数字还是科学计数, 默认false, 即是否以toPlainString()科学计数方式来输出
			builder.featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
	
			List<com.fasterxml.jackson.databind.Module> modules = new ArrayList<>();
			// 长整型数字转字符串
			builder.serializerByType(Long.class, ToStringSerializer.instance);
			builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
			builder.serializerByType(BigInteger.class, ToStringSerializer.instance);
			builder.serializerByType(BigDecimal.class, ToStringSerializer.instance);
			//识别Java8时间
			modules.add(new Jdk8Module());
	        JavaTimeModule javaTimeModule = new JavaTimeModule();
	        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)))
	                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateTimeFormat)));
			modules.add(javaTimeModule);
	        //识别Guava包的类
	        modules.add(new GuavaModule());
	        builder.modulesToInstall(modules.toArray(com.fasterxml.jackson.databind.Module[]::new));
		};
	}
}
