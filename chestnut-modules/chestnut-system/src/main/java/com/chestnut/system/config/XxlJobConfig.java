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
package com.chestnut.system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobConfig {

	private final XxlJobProperties xxlJobProperties;
	
	@Bean
	@ConditionalOnProperty(prefix = "xxl.job", name = "enable", havingValue = "true")
	public XxlJobSpringExecutor xxlJobExecutor() {
		if (StringUtils.isEmpty(this.xxlJobProperties.getAdminAddresses())) {
			return null;
		}
		log.info(">>>>>>>>>>> xxl-job config init.");
		XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
		xxlJobSpringExecutor.setAccessToken(this.xxlJobProperties.getAccessToken());
		xxlJobSpringExecutor.setAdminAddresses(this.xxlJobProperties.getAdminAddresses());

		xxlJobSpringExecutor.setAppname(this.xxlJobProperties.getExecutor().getAppName()); // 执行器AppName：执行器心跳注册分组依据；为空则关闭自动注册
		xxlJobSpringExecutor.setAddress(this.xxlJobProperties.getExecutor().getAddress());
		xxlJobSpringExecutor.setIp(this.xxlJobProperties.getExecutor().getIp());
		xxlJobSpringExecutor.setPort(this.xxlJobProperties.getExecutor().getPort());
		xxlJobSpringExecutor.setLogPath(this.xxlJobProperties.getExecutor().getLogPath());
		xxlJobSpringExecutor.setLogRetentionDays(this.xxlJobProperties.getExecutor().getLogRetentionDays());
		return xxlJobSpringExecutor;
	}
	
	/**
	 * 本地轻量级定时任务支持
	 */
	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        threadPoolTaskScheduler.setThreadNamePrefix("xyTaskScheduler-");
        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskScheduler;
    }
}