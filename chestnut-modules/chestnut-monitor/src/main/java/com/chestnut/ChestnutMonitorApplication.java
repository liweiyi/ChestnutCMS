package com.chestnut;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class ChestnutMonitorApplication {
	
	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		SpringApplication.run(ChestnutMonitorApplication.class, args);
		System.out.println("ChestnutMonitorApplication startup, cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
