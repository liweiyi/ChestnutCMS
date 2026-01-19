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
package com.chestnut.contentcore.publish.strategies;

import com.chestnut.contentcore.config.properties.CMSPublishProperties;
import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.IStaticizeType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 发布策略：ThreadPool
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = CMSPublishProperties.PREFIX, name = "strategy", havingValue = ThreadPoolPublishStrategy.ID)
public class ThreadPoolPublishStrategy implements IPublishStrategy, CommandLineRunner {

    public static final String ID = "ThreadPool";

    private final CMSPublishProperties properties;

    private final CmsStaticizeService cmsStaticizeService;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void publish(String dataType, String dataId) {
        IStaticizeType staticizeType = cmsStaticizeService.getStaticizeType(dataType);
        if (Objects.nonNull(staticizeType)) {
            threadPoolTaskExecutor.execute(() -> staticizeType.staticize(dataId));
        }
    }

    @Override
    public long getTaskCount() {
        // 返回线程池队列信息
        return threadPoolTaskExecutor.getQueueSize();
    }

    @Override
    public void cleanTasks() {
        // 清空线程池队列
        threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().clear();
    }

    @Override
    public void run(String... args) throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(properties.getPool().getThreadNamePrefix());
        executor.setCorePoolSize(properties.getPool().getCoreSize());
        executor.setQueueCapacity(properties.getPool().getQueueCapacity());
        executor.setMaxPoolSize(properties.getPool().getMaxSize());
        executor.setKeepAliveSeconds((int) properties.getPool().getKeepAlive().getSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 默认池满使用调用线程执行
        executor.setAllowCoreThreadTimeOut(this.properties.getPool().isAllowCoreThreadTimeout());
        executor.setWaitForTasksToCompleteOnShutdown(properties.getShutdown().isAwaitTermination());
        executor.setAwaitTerminationSeconds((int) properties.getShutdown().getAwaitTerminationPeriod().toSeconds());
        executor.initialize();
        this.threadPoolTaskExecutor = executor;
    }
}
