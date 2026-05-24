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
import org.springframework.util.ErrorHandler;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicLong;

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
        // 订阅级 poll 异常处理器：所有 consumer 共享一个实例，以便限频状态一致
        ErrorHandler pollErrorHandler = new KeepAliveErrorHandler();
        // 监听容器配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10) // 一次拉取消息数量
                .pollTimeout(Duration.ofSeconds(3)) // 拉取消息超时时间
                .executor(createThreadPoolTaskExecutor())
                // 兜底 errorHandler：未显式指定 errorHandler 的订阅会使用该默认值
                .errorHandler(pollErrorHandler)
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

            // 使用 lastConsumed()：先读取 pending 消息（已消费但未确认），然后读取新消息
            // cancelOnError(false) 关键：poll 阶段任何异常都不取消订阅，避免一次性异常导致静默停消费
            StreamOffset<String> offset = StreamOffset.create(PUBLISH_STREAM_NAME, ReadOffset.lastConsumed());
            StreamMessageListenerContainer.ConsumerStreamReadRequest<String> readRequest = StreamMessageListenerContainer.StreamReadRequest
                    .<String>builder(offset)
                    .consumer(consumer)
                    .autoAcknowledge(false) // ACK 交由 PublishTaskReceiver 在处理完成后执行
                    .errorHandler(pollErrorHandler)
                    .cancelOnError(t -> false)
                    .build();
            container.register(readRequest, publishTaskReceiver);
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

    /**
     * 发布流 poll 异常处理器：
     * 1) 中断/取消类异常（容器 stop 时的正常信号）降级为 debug 日志；
     * 2) 同一异常类型在 {@link #SUPPRESS_WINDOW_NANOS} 窗口内抑制打印，避免持续性异常刷屏；
     * 3) 首次出现或跨窗口的异常打 ERROR，保留排障现场。
     */
    static final class KeepAliveErrorHandler implements ErrorHandler {

        private static final long SUPPRESS_WINDOW_NANOS = Duration.ofMinutes(1).toNanos();

        private final AtomicLong lastLogNanos = new AtomicLong(Long.MIN_VALUE);
        private volatile String lastExceptionKey;

        @Override
        public void handleError(Throwable t) {
            if (isCancelSignal(t)) {
                log.debug("Publish stream poll interrupted/cancelled (expected on shutdown): {}", t.toString());
                return;
            }
            String key = t.getClass().getName();
            long now = System.nanoTime();
            long last = lastLogNanos.get();
            boolean sameKey = Objects.equals(key, lastExceptionKey);
            if (sameKey && (now - last) < SUPPRESS_WINDOW_NANOS) {
                return;
            }
            if (lastLogNanos.compareAndSet(last, now)) {
                lastExceptionKey = key;
                log.error("Publish stream poll error, keep subscription alive (subsequent identical errors within 1min suppressed)", t);
            }
        }

        private static boolean isCancelSignal(Throwable t) {
            Throwable cur = t;
            while (cur != null) {
                if (cur instanceof InterruptedException || cur instanceof CancellationException) {
                    return true;
                }
                cur = cur.getCause();
            }
            return false;
        }
    }
}
