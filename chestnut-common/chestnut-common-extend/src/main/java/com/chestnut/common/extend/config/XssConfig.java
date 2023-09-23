package com.chestnut.common.extend.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chestnut.common.extend.config.properties.XssProperties;
import com.chestnut.common.extend.xss.XssDeserializer;
import com.chestnut.common.extend.xss.XssInterceptor;
import com.chestnut.common.utils.StringUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(XssProperties.class)
public class XssConfig implements WebMvcConfigurer {

	private final XssProperties xssProperties;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (xssProperties.isEnabled()) {
			List<String> urlPatterns = xssProperties.getUrlPatterns();
			if (StringUtils.isEmpty(urlPatterns)) {
				urlPatterns.add("/**");
			}
			List<String> excludes = xssProperties.getExcludes();
			if (StringUtils.isEmpty(excludes)) {
				excludes = Collections.emptyList();
			}
			registry.addInterceptor(new XssInterceptor()).addPathPatterns(urlPatterns)
					.excludePathPatterns(excludes).order(Ordered.LOWEST_PRECEDENCE);
		}
	}
	
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.deserializerByType(String.class,
				new XssDeserializer(xssProperties.getMode()));
	}
}