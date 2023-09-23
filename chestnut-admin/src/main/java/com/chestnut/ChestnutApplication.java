package com.chestnut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SpringBootApplication
public class ChestnutApplication {
	
	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(ChestnutApplication.class, args);
		System.out.println("ChestnutApplication startup, cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
