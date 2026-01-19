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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 发布策略：Redis Stream
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = CMSPublishProperties.PREFIX, name = "strategy", havingValue = RedisStreamPublishStrategy.ID)
public class RedisStreamPublishStrategy implements IPublishStrategy {

    public static final String ID = "RedisStream";

    public static final String PUBLISH_STREAM_NAME = "ChestnutCMSPublishStream";

    public static final String PUBLISH_CONSUMER_GROUP = "ChestnutCMSPublishConsumerGroup";

    private final CMSPublishProperties properties;

    private final StringRedisTemplate redisTemplate;

    private final CmsStaticizeService cmsStaticizeService;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void publish(String dataType, String dataId) {
        MapRecord<String, String, String> record = MapRecord.create(PUBLISH_STREAM_NAME, Map.of(
                "type", dataType,
                "id", dataId
        ));
        redisTemplate.opsForStream().add(record);
    }

    @Override
    public long getTaskCount() {
        StreamInfo.XInfoStream info = redisTemplate.opsForStream().info(PUBLISH_STREAM_NAME);
        return info.streamLength();
    }

    @Override
    public void cleanTasks() {
		try {
            // 先删除消费者组
            try {
                redisTemplate.opsForStream().destroyGroup(PUBLISH_STREAM_NAME, PUBLISH_CONSUMER_GROUP);
            } catch (Exception e) {
                log.debug("Publish task consumer group does not exist or delete failed: {}", e.getMessage());
            }
            // 删除stream
            redisTemplate.delete(PUBLISH_STREAM_NAME);
            // 等待一下确保删除完成
            Thread.sleep(100);
            // 重新创建消费者组
            redisTemplate.opsForStream().createGroup(PUBLISH_STREAM_NAME, PUBLISH_CONSUMER_GROUP);
            log.info("Publish task consumer group created: {}", PUBLISH_CONSUMER_GROUP);
		} catch (Exception e) {
			log.error("Failed to clean publish tasks", e);
		}
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer() {
        // 启动清理消息队列数据
        if (properties.isClearOnStart()) {
            redisTemplate.delete(PUBLISH_STREAM_NAME);
        }
        // 监听容器配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10) // 一次拉取消息数量
                .pollTimeout(Duration.ofSeconds(3)) // 拉取消息超时时间
                .executor(createThreadPoolTaskExecutor())
                .build();
        // 创建监听容器
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer
                .create(redisTemplate.getRequiredConnectionFactory(), streamMessageListenerContainerOptions);
        //创建消费者组
        try {
            redisTemplate.opsForStream().createGroup(PUBLISH_STREAM_NAME, PUBLISH_CONSUMER_GROUP);
            log.info("Publish task consumer group created: {}", PUBLISH_CONSUMER_GROUP);
        } catch (Exception e) {
            log.info("Publish task consumer group:{} already exists", PUBLISH_CONSUMER_GROUP);
        }
        // 添加消费者
        for (int i = 0; i < properties.getConsumerCount(); i++) {
            Consumer consumer = Consumer.from(PUBLISH_CONSUMER_GROUP, "cms-publish-consumer-" + i);
            PublishTaskReceiver publishTaskReceiver = new PublishTaskReceiver(cmsStaticizeService, redisTemplate);
            publishTaskReceiver.setConsumer(consumer);
            
            // 使用lastConsumed()：会先读取pending消息（已消费但未确认），然后读取新消息
            // 这样可以确保pending消息被优先处理，同时也能处理新消息
            container.receive(consumer, StreamOffset.create(PUBLISH_STREAM_NAME, ReadOffset.lastConsumed()), publishTaskReceiver);
            log.info("Start publish task consumer: {} (listen pending and new messages)", consumer.getName());
        }
        container.start();
        return container;
    }

    private ThreadPoolTaskExecutor createThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(properties.getPool().getThreadNamePrefix());
        executor.setCorePoolSize(properties.getPool().getCoreSize());
        executor.setQueueCapacity(properties.getPool().getQueueCapacity());
        executor.setMaxPoolSize(properties.getPool().getMaxSize());
        executor.setKeepAliveSeconds((int) properties.getPool().getKeepAlive().getSeconds());
        executor.setAllowCoreThreadTimeOut(this.properties.getPool().isAllowCoreThreadTimeout());
        executor.setWaitForTasksToCompleteOnShutdown(properties.getShutdown().isAwaitTermination());
        executor.setAwaitTerminationSeconds((int) properties.getShutdown().getAwaitTerminationPeriod().toSeconds());
        log.info("Cms publish task executor initialize: {}", executor.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }
}
