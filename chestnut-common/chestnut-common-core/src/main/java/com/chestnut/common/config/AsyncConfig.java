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

import com.chestnut.common.config.properties.AsyncProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Configuration
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
@EnableConfigurationProperties(AsyncProperties.class)
public class AsyncConfig implements AsyncConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

	public static final String COMMON_EXECUTOR_BEAN = "chestnutTaskExecutor";

	private final AsyncProperties properties;
	
	@Bean(COMMON_EXECUTOR_BEAN)
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix(properties.getPool().getThreadNamePrefix());
		// 常驻内存核心线程数
		executor.setCorePoolSize(properties.getPool().getCoreSize());
		// 任务队列长度，队列满后扩展核心线程
		executor.setQueueCapacity(properties.getPool().getQueueCapacity());
		// 最大线程数
		executor.setMaxPoolSize(properties.getPool().getMaxSize());
		// 空闲线程存活时间
		executor.setKeepAliveSeconds((int) properties.getPool().getKeepAlive().getSeconds());
		executor.setAllowCoreThreadTimeOut(this.properties.getPool().isAllowCoreThreadTimeout());
		// 关闭服务是否等待未完成任务执行完
		executor.setWaitForTasksToCompleteOnShutdown(properties.getShutdown().isAwaitTermination());
		// 关闭服务等待未完成任务最大时长
		executor.setAwaitTerminationSeconds((int) properties.getShutdown().getAwaitTerminationPeriod().toSeconds());
		/*
		 * 当线程池线程数已经达到maxSize时的处理策略：
		 * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。 默认策略
		 * ThreadPoolExecutor.DiscardPolicy：丢弃任务，但是不抛出异常。
		 * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
		 * ThreadPoolExecutor.CallerRunsPolicy：不创建新线程执行任务，由调用线程直接处理该任务
		 */
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		logger.info("Chestnut async task executor initialize: {}", executor.getThreadNamePrefix());
		executor.initialize();
		return executor;
	}
}