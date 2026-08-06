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
package com.chestnut.common.utils;

import com.chestnut.common.utils.file.FileExUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {

	/** Spring应用上下文环境 */
	private static ConfigurableListableBeanFactory beanFactory;

	private static ApplicationContext applicationContext;

	@Override
	public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
		SpringUtils.beanFactory = beanFactory;
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		SpringUtils.applicationContext = applicationContext;
	}

	public static void publishEvent(ApplicationEvent event) {
		applicationContext.publishEvent(event);
	}

	/**
	 * 获取对象
	 *
	 * @param name bean的名字
	 * @return Object 一个以所给名字注册的bean的实例
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		return (T) beanFactory.getBean(name);
	}

	/**
	 * 获取类型为requiredType的对象
	 *
	 * @param clz JAVA类型
	 * @return T 一个T类型的BEAN对象
     *
	 */
	public static <T> T getBean(Class<T> clz) {
        return beanFactory.getBean(clz);
	}

	public static <T> Map<String, T> getBeanMap(Class<T> clz) {
		return beanFactory.getBeansOfType(clz);
	}

	/**
	 * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
	 *
	 * @param name bean的名字
	 * @return boolean
	 */
	public static boolean containsBean(String name) {
		return beanFactory.containsBean(name);
	}

	/**
	 * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
	 * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
	 *
	 * @param name bean的名字
	 * @return boolean
     *
	 */
	public static boolean isSingleton(String name) {
		return beanFactory.isSingleton(name);
	}

	/**
	 * @param type bean类型
	 * @return Class 注册对象的类型
	 *
	 */
	public static Class<?> getType(String type) {
		return beanFactory.getType(type);
	}

	/**
	 * 如果给定的bean名字在bean定义中有别名，则返回这些别名
	 *
	 * @param name bean的名字
	 * @return String[] 包含此bean的别名的String数组
	 *
	 */
	public static String[] getAliases(String name) {
		return beanFactory.getAliases(name);
	}

	/**
	 * 获取aop代理对象
	 * 
	 * @param invoker 调用者
	 * @return T 代理对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAopProxy(T invoker) {
		return (T) AopContext.currentProxy();
	}

	/**
	 * 获取当前的环境配置，无配置返回null
	 *
	 * @return 当前的环境配置
	 */
	public static String[] getActiveProfiles() {
		return applicationContext.getEnvironment().getActiveProfiles();
	}

	/**
	 * 获取当前的环境配置，当有多个环境配置时，只获取第一个
	 *
	 * @return 当前的环境配置
	 */
	public static String getActiveProfile() {
		final String[] activeProfiles = getActiveProfiles();
		return StringUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
	}

	/**
	 * 获取配置文件中的值
	 *
	 * @param key 配置文件的key
	 * @return 当前的配置文件的值
	 *
	 */
	public static String getRequiredProperty(String key) {
		return applicationContext.getEnvironment().getRequiredProperty(key);
	}

	/**
	 * 获取应用当前所在目录
	 */
	public static String getAppParentDirectory() {
		ApplicationHome applicationHome = new ApplicationHome(SpringUtils.class);
		File applicationDir = applicationHome.getSource();
		if (applicationDir == null) {
			applicationDir = new File(System.getProperty("user.dir"));
		}
		String[] activeProfiles = getActiveProfiles();
		if (ArrayUtils.indexOf("dev", activeProfiles) > -1) {
			String dirPath = FileExUtils.normalizePath(applicationDir.getAbsolutePath());
			applicationDir = new File(StringUtils.substringBefore(dirPath, "/chestnut-common/"));
		}
		String dir = FileExUtils.normalizePath(applicationDir.getParentFile().getAbsolutePath());
		if (dir.contains("/BOOT-INF/lib")) {
			dir = StringUtils.substringBefore(dir, "/BOOT-INF/lib"); 
		}
		return dir;
	}

	public static boolean isDevelopment() {
		return applicationContext.getEnvironment().acceptsProfiles(Profiles.of("dev"));
	}

	public static boolean isProduction() {
		return applicationContext.getEnvironment().acceptsProfiles(Profiles.of("prod"));
	}
}
