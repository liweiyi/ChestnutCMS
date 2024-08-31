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
package com.chestnut.contentcore.publish.strategies;

import com.chestnut.common.async.AsyncTask;
import com.chestnut.contentcore.config.properties.CMSPublishProperties;
import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.IStaticizeType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * 发布策略：Redis Set
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = CMSPublishProperties.PREFIX, name = "strategy", havingValue = RedisSetPublishStrategy.ID)
public class RedisSetPublishStrategy implements IPublishStrategy, CommandLineRunner {

    public static final String ID = "RedisSet";

    static final String CACHE_NAME = "cms:publish:list";

    private final StringRedisTemplate redisTemplate;

    private final CMSPublishProperties properties;

    private final CmsStaticizeService cmsStaticizeService;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void publish(String dataType, String dataId) {
        redisTemplate.opsForSet().add(CACHE_NAME, dataType+"-"+dataId);
    }

    @Override
    public long getTaskCount() {
        Long size = redisTemplate.opsForSet().size(CACHE_NAME);
        return Objects.requireNonNullElse(size, 0L);
    }

    @Override
    public void cleanTasks() {
        redisTemplate.opsForSet().remove(CACHE_NAME);
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 1; i <= properties.getConsumerCount(); i++) {
            // 创建一个3秒间隔执行的定时任务
            PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofSeconds(3L));
            periodicTrigger.setFixedRate(false);

            AsyncTask task = new AsyncTask() {

                @Override
                public void run0() {
                    String data = null;
                    do {
                        try {
                            data = redisTemplate.opsForSet().pop(CACHE_NAME);
                            if (Objects.nonNull(data)) {
                                String[] split = data.split("-");
                                String dataType = split[0];
                                String dataId = split[1];

                                IStaticizeType staticizeType = cmsStaticizeService.getStaticizeType(dataType);
                                if (Objects.nonNull(staticizeType)) {
                                    staticizeType.staticize(dataId);
                                }
                            }
                        } catch (Exception e) {
                            IStaticizeType.logger.error("静态化失败", e);
                        }
                    } while(Objects.nonNull(data));
                }
            };
            task.setTaskId("cms-publish-" + i);
            task.setType("CMS-PUBLISH");
            threadPoolTaskScheduler.schedule(task, periodicTrigger);
        }
    }
}
