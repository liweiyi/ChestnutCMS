package com.chestnut.generator.util;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.velocity.app.Velocity;

/**
 * VelocityEngine工厂
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class VelocityInitializer {
	
	/**
	 * 初始化vm方法
	 */
	public static void initVelocity() {
		Properties p = new Properties();
		try {
			// 加载classpath目录下的vm文件
			p.setProperty("resource.loader.file.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			// 定义字符集
			p.setProperty(Velocity.INPUT_ENCODING, StandardCharsets.UTF_8.name());
			// 初始化Velocity引擎，指定配置Properties
			Velocity.init(p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
