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
package com.chestnut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

/**
 * 启动程序
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SpringBootApplication
public class ChestnutApplication {
	
	public static void main(String[] args) throws Exception {
		long s = System.currentTimeMillis();
		System.setProperty("spring.devtools.restart.enabled", "false");
		System.setProperty("LOCAL_IP", InetAddress.getLocalHost().getHostAddress());
		SpringApplication.run(ChestnutApplication.class, args);
		System.out.println("ChestnutApplication startup, cost: " + (System.currentTimeMillis() - s) + "ms");
	}
}
